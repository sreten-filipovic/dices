create function public.my_active_game()
returns uuid
language sql
stable
security definer
set search_path = ''
as $$
  select r.id
  from public.rooms r
  join public.room_members m on m.room_id = r.id
  where m.user_id = (select auth.uid())
    and m.status = 'accepted'
    and r.status = 'playing'
    and not exists (
      select 1 from public.game_events e
      where e.room_id = r.id and e.user_id = m.user_id and e.type = 'leave'
    )
    and coalesce(
      (select max(e.created_at) from public.game_events e where e.room_id = r.id),
      r.started_at
    ) > now() - interval '30 minutes'
  order by r.started_at desc
  limit 1;
$$;

revoke execute on function public.my_active_game() from public, anon;
grant execute on function public.my_active_game() to authenticated;
