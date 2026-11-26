create or replace function public.submit_action(p_room uuid, p_action text)
returns int
language plpgsql
security definer
set search_path = ''
as $$
begin
  if p_action not in ('keep', 'stand', 'bank', 'stop', 'finish', 'rematch') then
    raise exception 'not_allowed' using errcode = 'P0001';
  end if;
  return public.append_game_event(
    p_room, public.require_player(p_room), 'action', jsonb_build_object('name', p_action)
  );
end;
$$;
