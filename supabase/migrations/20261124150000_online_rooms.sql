create table public.rooms (
  id uuid primary key default gen_random_uuid(),
  code text not null,
  game text not null check (game in ('ludo')),
  host_id uuid not null references public.profiles (id) on delete cascade,
  max_players int not null check (max_players between 2 and 4),
  status text not null default 'open' check (status in ('open', 'playing', 'finished', 'closed')),
  created_at timestamptz not null default now(),
  last_activity_at timestamptz not null default now(),
  host_seen_at timestamptz not null default now(),
  started_at timestamptz
);

create unique index rooms_active_code_unique on public.rooms (code) where status in ('open', 'playing');
create index rooms_open_by_game on public.rooms (game, created_at desc) where status = 'open';

create table public.room_members (
  room_id uuid not null references public.rooms (id) on delete cascade,
  user_id uuid not null references public.profiles (id) on delete cascade,
  username text not null,
  status text not null check (status in ('pending', 'accepted', 'rejected', 'left', 'kicked')),
  seat int,
  requested_at timestamptz not null default now(),
  primary key (room_id, user_id)
);

create index room_members_user on public.room_members (user_id);

create table public.game_events (
  room_id uuid not null references public.rooms (id) on delete cascade,
  seq int not null,
  user_id uuid not null,
  type text not null check (type in ('roll', 'move', 'leave', 'timeout')),
  payload jsonb not null default '{}',
  created_at timestamptz not null default now(),
  primary key (room_id, seq)
);

alter table public.rooms enable row level security;
alter table public.room_members enable row level security;
alter table public.game_events enable row level security;

revoke all on public.rooms, public.room_members, public.game_events from anon, authenticated;
grant select on public.rooms, public.room_members, public.game_events to authenticated;

create function public.is_room_participant(p_room uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (select 1 from public.rooms where id = p_room and host_id = (select auth.uid()))
      or exists (select 1 from public.room_members where room_id = p_room and user_id = (select auth.uid()));
$$;

create policy "Participants read their rooms"
  on public.rooms for select to authenticated
  using (public.is_room_participant(id));

create policy "Participants read room members"
  on public.room_members for select to authenticated
  using (public.is_room_participant(room_id));

create policy "Participants read game events"
  on public.game_events for select to authenticated
  using (public.is_room_participant(room_id));

alter publication supabase_realtime add table public.rooms, public.room_members, public.game_events;

create function public.room_is_alive(r public.rooms)
returns boolean
language sql
stable
set search_path = ''
as $$
  select r.status = 'open'
     and r.host_seen_at > now() - interval '45 seconds'
     and r.last_activity_at > now() - interval '10 minutes';
$$;

create function public.require_user()
returns uuid
language plpgsql
stable
set search_path = ''
as $$
begin
  if auth.uid() is null then
    raise exception 'not_signed_in' using errcode = 'P0001';
  end if;
  return auth.uid();
end;
$$;

create function public.generate_room_code()
returns text
language plpgsql
set search_path = ''
as $$
declare
  alphabet constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  result text;
begin
  loop
    result := '';
    for i in 1..6 loop
      result := result || substr(alphabet, 1 + floor(random() * length(alphabet))::int, 1);
    end loop;
    exit when not exists (
      select 1 from public.rooms where code = result and status in ('open', 'playing')
    );
  end loop;
  return result;
end;
$$;

create function public.create_room(p_game text, p_max_players int)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
  my_name text;
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

  insert into public.rooms (code, game, host_id, max_players)
    values (public.generate_room_code(), p_game, me, p_max_players)
    returning id into new_room;

  insert into public.room_members (room_id, user_id, username, status)
    values (new_room, me, my_name, 'accepted');

  return new_room;
end;
$$;

create function public.list_open_rooms(p_game text)
returns table (id uuid, code text, host_name text, max_players int, player_count int, created_at timestamptz)
language sql
stable
security definer
set search_path = ''
as $$
  select r.id, r.code, p.username, r.max_players,
         (select count(*)::int from public.room_members m where m.room_id = r.id and m.status = 'accepted'),
         r.created_at
  from public.rooms r
  join public.profiles p on p.id = r.host_id
  where r.game = p_game and public.room_is_alive(r)
  order by r.created_at desc
  limit 50;
$$;

create function public.find_room(p_code text)
returns uuid
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
  match_id uuid;
begin
  perform public.require_user();
  select r.id into match_id from public.rooms r
    where r.code = upper(trim(p_code)) and public.room_is_alive(r);
  if match_id is null then
    raise exception 'room_not_found' using errcode = 'P0001';
  end if;
  return match_id;
end;
$$;

create function public.request_join(p_room uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
  my_name text;
  r public.rooms;
  existing text;
begin
  select * into r from public.rooms where id = p_room for update;
  if r.id is null or not public.room_is_alive(r) then
    raise exception 'room_not_found' using errcode = 'P0001';
  end if;
  if r.host_id = me then
    return;
  end if;

  select status into existing from public.room_members where room_id = p_room and user_id = me;
  if existing in ('pending', 'accepted') then
    return;
  end if;
  if existing in ('rejected', 'kicked') then
    raise exception 'request_rejected' using errcode = 'P0001';
  end if;
  if (select count(*) from public.room_members where room_id = p_room and status = 'accepted') >= r.max_players then
    raise exception 'room_full' using errcode = 'P0001';
  end if;

  update public.room_members m set status = 'left'
    from public.rooms other
    where m.room_id = other.id and m.user_id = me and m.status = 'pending'
      and other.status = 'open' and other.id <> p_room;

  select username into my_name from public.profiles where id = me;
  insert into public.room_members (room_id, user_id, username, status)
    values (p_room, me, my_name, 'pending')
    on conflict (room_id, user_id)
    do update set status = 'pending', username = excluded.username, requested_at = now();

  update public.rooms set last_activity_at = now() where id = p_room;
end;
$$;

create function public.respond_join(p_room uuid, p_user uuid, p_accept boolean)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
  r public.rooms;
begin
  select * into r from public.rooms where id = p_room for update;
  if r.id is null or r.host_id <> me or r.status <> 'open' then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  if not exists (select 1 from public.room_members where room_id = p_room and user_id = p_user and status = 'pending') then
    return;
  end if;
  if p_accept and (select count(*) from public.room_members where room_id = p_room and status = 'accepted') >= r.max_players then
    raise exception 'room_full' using errcode = 'P0001';
  end if;

  update public.room_members
    set status = case when p_accept then 'accepted' else 'rejected' end
    where room_id = p_room and user_id = p_user;
  update public.rooms set last_activity_at = now() where id = p_room;
end;
$$;

create function public.kick_member(p_room uuid, p_user uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
begin
  if not exists (select 1 from public.rooms where id = p_room and host_id = me and status = 'open') or p_user = me then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  update public.room_members set status = 'kicked'
    where room_id = p_room and user_id = p_user and status = 'accepted';
end;
$$;

create function public.touch_room(p_room uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
  update public.rooms set host_seen_at = now()
    where id = p_room and host_id = public.require_user() and status = 'open';
end;
$$;

create function public.append_game_event(p_room uuid, p_user uuid, p_type text, p_payload jsonb)
returns int
language plpgsql
security definer
set search_path = ''
as $$
declare
  next_seq int;
begin
  perform 1 from public.rooms where id = p_room for update;
  select coalesce(max(seq), 0) + 1 into next_seq from public.game_events where room_id = p_room;
  insert into public.game_events (room_id, seq, user_id, type, payload)
    values (p_room, next_seq, p_user, p_type, p_payload);
  return next_seq;
end;
$$;

create function public.require_player(p_room uuid)
returns uuid
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
begin
  if not exists (
    select 1 from public.rooms r
    join public.room_members m on m.room_id = r.id
    where r.id = p_room and r.status = 'playing' and m.user_id = me and m.status = 'accepted'
  ) then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return me;
end;
$$;

create function public.leave_room(p_room uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
  r public.rooms;
begin
  select * into r from public.rooms where id = p_room;
  if r.id is null then
    return;
  end if;
  if r.status = 'open' then
    if r.host_id = me then
      update public.rooms set status = 'closed' where id = p_room;
    else
      update public.room_members set status = 'left'
        where room_id = p_room and user_id = me and status in ('pending', 'accepted');
    end if;
  elsif r.status = 'playing' and exists (
    select 1 from public.room_members where room_id = p_room and user_id = me and status = 'accepted'
  ) then
    perform public.append_game_event(p_room, me, 'leave', '{}'::jsonb);
  end if;
end;
$$;

create function public.start_room(p_room uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_user();
  r public.rooms;
begin
  select * into r from public.rooms where id = p_room for update;
  if r.id is null or r.host_id <> me or r.status <> 'open' then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  if (select count(*) from public.room_members where room_id = p_room and status = 'accepted') < 2 then
    raise exception 'not_enough_players' using errcode = 'P0001';
  end if;

  update public.room_members m set seat = ordered.seat
    from (
      select user_id, (row_number() over (order by (user_id = r.host_id) desc, requested_at) - 1)::int as seat
      from public.room_members
      where room_id = p_room and status = 'accepted'
    ) ordered
    where m.room_id = p_room and m.user_id = ordered.user_id;
  update public.room_members set status = 'rejected' where room_id = p_room and status = 'pending';
  update public.rooms set status = 'playing', started_at = now() where id = p_room;
end;
$$;

create function public.submit_roll(p_room uuid)
returns int
language plpgsql
security definer
set search_path = ''
as $$
begin
  return public.append_game_event(
    p_room, public.require_player(p_room), 'roll',
    jsonb_build_object('value', 1 + floor(random() * 6)::int)
  );
end;
$$;

create function public.submit_move(p_room uuid, p_pawn int)
returns int
language plpgsql
security definer
set search_path = ''
as $$
begin
  if p_pawn not between 0 and 3 then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return public.append_game_event(
    p_room, public.require_player(p_room), 'move', jsonb_build_object('pawn', p_pawn)
  );
end;
$$;

create function public.submit_timeout(p_room uuid, p_after_seq int)
returns int
language plpgsql
security definer
set search_path = ''
as $$
declare
  me uuid := public.require_player(p_room);
  last_seq int;
  last_at timestamptz;
begin
  perform 1 from public.rooms where id = p_room for update;
  select seq, created_at into last_seq, last_at
    from public.game_events where room_id = p_room order by seq desc limit 1;
  if coalesce(last_seq, 0) <> p_after_seq then
    return null;
  end if;
  if last_at is null then
    select started_at into last_at from public.rooms where id = p_room;
  end if;
  if last_at > now() - interval '60 seconds' then
    raise exception 'too_early' using errcode = 'P0001';
  end if;
  return public.append_game_event(
    p_room, me, 'timeout',
    jsonb_build_object('rolls', (select jsonb_agg(1 + floor(random() * 6)::int) from generate_series(1, 12)))
  );
end;
$$;

create function public.finish_room(p_room uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
  update public.rooms set status = 'finished'
    where id = p_room and status = 'playing' and public.is_room_participant(p_room);
end;
$$;

create function public.get_room(p_room uuid)
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

revoke execute on all functions in schema public from public, anon;
grant execute on function public.is_username_available(text) to anon, authenticated;
revoke execute on function
  public.append_game_event(uuid, uuid, text, jsonb),
  public.generate_room_code(),
  public.room_is_alive(public.rooms)
  from authenticated;
grant execute on function
  public.create_room(text, int),
  public.list_open_rooms(text),
  public.find_room(text),
  public.request_join(uuid),
  public.respond_join(uuid, uuid, boolean),
  public.kick_member(uuid, uuid),
  public.touch_room(uuid),
  public.leave_room(uuid),
  public.start_room(uuid),
  public.submit_roll(uuid),
  public.submit_move(uuid, int),
  public.submit_timeout(uuid, int),
  public.finish_room(uuid),
  public.get_room(uuid),
  public.is_room_participant(uuid),
  public.require_user(),
  public.require_player(uuid)
  to authenticated;
