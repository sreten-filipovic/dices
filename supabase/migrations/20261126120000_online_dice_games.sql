alter table public.rooms drop constraint rooms_game_check;
alter table public.rooms add constraint rooms_game_check
  check (game in ('ludo', 'dice_poker', 'dice_duel', 'dice_jack', 'sevens', 'risk_it', 'straight_roll'));

alter table public.rooms drop constraint rooms_max_players_check;
alter table public.rooms add constraint rooms_max_players_check
  check (max_players between 2 and 6);

alter table public.rooms add column settings jsonb not null default '{}';

alter table public.game_events drop constraint game_events_type_check;
alter table public.game_events add constraint game_events_type_check
  check (type in ('roll', 'move', 'leave', 'timeout', 'dice', 'reroll', 'action'));

create function public.validate_room_settings(p_game text, p_max_players int, p_settings jsonb)
returns jsonb
language plpgsql
immutable
set search_path = ''
as $$
declare
  rounds int := (p_settings ->> 'rounds')::int;
  dice_count int := (p_settings ->> 'dice_count')::int;
  player_limit int := 6;
begin
  if p_game not in ('ludo', 'dice_poker', 'dice_duel', 'dice_jack', 'sevens', 'risk_it', 'straight_roll') then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  if p_game = 'ludo' then
    player_limit := 4;
  end if;
  if p_max_players < 2 or p_max_players > player_limit then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  if p_game = 'ludo' then
    return '{}'::jsonb;
  end if;
  if p_game = 'dice_duel' then
    if dice_count is null or dice_count not between 1 and 3 then
      raise exception 'not_allowed' using errcode = 'P0001';
    end if;
    return jsonb_build_object('dice_count', dice_count);
  end if;
  if rounds is null or rounds not in (1, 3, 5) then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return jsonb_build_object('rounds', rounds);
end;
$$;

drop function public.create_room(text, int);

create function public.create_room(p_game text, p_max_players int, p_settings jsonb)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
  my_name text;
  clean_settings jsonb := public.validate_room_settings(p_game, p_max_players, coalesce(p_settings, '{}'::jsonb));
  new_room uuid;
begin
  select username into my_name from public.profiles where id = me;
  if my_name is null then
    raise exception 'not_signed_in' using errcode = 'P0001';
  end if;

  update public.rooms set status = 'closed' where host_id = me and status = 'open';
  update public.room_members m set status = 'left'
    from public.rooms r
    where m.room_id = r.id and m.user_id = me and m.status = 'pending' and r.status = 'open';

  insert into public.rooms (code, game, host_id, max_players, settings)
    values (public.generate_room_code(), p_game, me, p_max_players, clean_settings)
    returning id into new_room;

  insert into public.room_members (room_id, user_id, username, status)
    values (new_room, me, my_name, 'accepted');

  return new_room;
end;
$$;

drop function public.list_open_rooms(text);

create function public.list_open_rooms(p_game text)
returns table (
  id uuid, code text, host_name text, max_players int, player_count int, created_at timestamptz, settings jsonb
)
language sql
stable
security definer
set search_path = ''
as $$
  select r.id, r.code, p.username, r.max_players,
         (select count(*)::int from public.room_members m where m.room_id = r.id and m.status = 'accepted'),
         r.created_at, r.settings
  from public.rooms r
  join public.profiles p on p.id = r.host_id
  where r.game = p_game and public.room_is_alive(r)
  order by r.created_at desc
  limit 50;
$$;

create or replace function public.get_room(p_room uuid)
returns jsonb
language sql
stable
security definer
set search_path = ''
as $$
  select jsonb_build_object(
    'id', r.id,
    'code', r.code,
    'game', r.game,
    'host_id', r.host_id,
    'max_players', r.max_players,
    'settings', r.settings,
    'status', case when r.status = 'open' and not public.room_is_alive(r) then 'closed' else r.status end,
    'members', coalesce((
      select jsonb_agg(jsonb_build_object(
        'user_id', m.user_id, 'username', m.username, 'status', m.status, 'seat', m.seat
      ) order by m.requested_at)
      from public.room_members m where m.room_id = r.id
    ), '[]'::jsonb)
  )
  from public.rooms r
  where r.id = p_room and public.is_room_participant(p_room);
$$;

create function public.random_dice(p_count int)
returns jsonb
language sql
volatile
set search_path = ''
as $$
  select coalesce(jsonb_agg(1 + floor(random() * 6)::int), '[]'::jsonb) from generate_series(1, p_count);
$$;

create function public.submit_dice(p_room uuid, p_count int)
returns int
language plpgsql
security definer
set search_path = ''
as $$
begin
  if p_count not between 1 and 6 then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return public.append_game_event(
    p_room, public.require_player(p_room), 'dice', jsonb_build_object('values', public.random_dice(p_count))
  );
end;
$$;

create function public.submit_reroll(p_room uuid, p_indices int[])
returns int
language plpgsql
security definer
set search_path = ''
as $$
declare
  clean int[];
begin
  select array_agg(distinct i order by i) into clean from unnest(p_indices) as i;
  if clean is null or cardinality(clean) > 6 or exists (select 1 from unnest(clean) as i where i not between 0 and 5) then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return public.append_game_event(
    p_room, public.require_player(p_room), 'reroll',
    jsonb_build_object('indices', to_jsonb(clean), 'values', public.random_dice(cardinality(clean)))
  );
end;
$$;

create function public.submit_action(p_room uuid, p_action text)
returns int
language plpgsql
security definer
set search_path = ''
as $$
begin
  if p_action not in ('keep', 'stand', 'bank', 'stop', 'finish') then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return public.append_game_event(
    p_room, public.require_player(p_room), 'action', jsonb_build_object('name', p_action)
  );
end;
$$;

revoke execute on function
  public.validate_room_settings(text, int, jsonb),
  public.random_dice(int)
  from public, anon, authenticated;

revoke execute on function
  public.create_room(text, int, jsonb),
  public.list_open_rooms(text),
  public.submit_dice(uuid, int),
  public.submit_reroll(uuid, int[]),
  public.submit_action(uuid, text)
  from public, anon;

grant execute on function
  public.create_room(text, int, jsonb),
  public.list_open_rooms(text),
  public.submit_dice(uuid, int),
  public.submit_reroll(uuid, int[]),
  public.submit_action(uuid, text)
  to authenticated;
