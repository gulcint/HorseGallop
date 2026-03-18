INSERT INTO tbf_activities (id, start_date, end_date, title, organization, city, discipline, activity_type) VALUES
('tbf-001','2026-03-19','2026-03-22','ANTALYA ATLI SPOR KULÜBÜ ENGEL ATLAMA YARIŞMALARI','ABAK','ANTALYA','show_jumping','incentive'),
('tbf-002','2026-03-21','2026-03-22','NEVRUZ KUPASI','HAL TEKE ATLI SPOR KULÜBÜ','ANKARA','show_jumping','cup'),
('tbf-003','2026-03-27','2026-03-29','TBF ULUSAL ATLI DAYANIKLILIK KALİFİKASYON','TBF','İSTANBUL','endurance','incentive'),
('tbf-004','2026-03-28','2026-03-29','TÜRKİYE LİGİ 3. AYAK','TBF','ANTALYA','show_jumping','incentive'),
('tbf-005','2026-04-04','2026-04-05','İSTANBUL AT TERBİYESİ ŞAMPİYONASI','ÖZEL MANEJ','İSTANBUL','dressage','championship'),
('tbf-006','2026-04-11','2026-04-12','EGE BÖLGE KUPASI','EGE ATLI','İZMİR','show_jumping','cup'),
('tbf-007','2026-04-18','2026-04-19','PONY LİG 2. AYAK','TBF','BURSA','pony','incentive'),
('tbf-008','2026-04-25','2026-04-26','ULUSLARARASI İSTANBUL CHS','TBF','İSTANBUL','show_jumping','international')
ON CONFLICT (id) DO NOTHING;
