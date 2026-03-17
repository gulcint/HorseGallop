# Claude Context Topology

Bu dizin, Claude subagent ve hook tabanli Android calisma akisi icin kanonik context alanidir.

## Yapilar

- `shared/`: task-dispatch'ten bagimsiz ortak kurallar
- `active/`: islenmekte olan gorevlere isaret eden kisa referanslar
- `archive/`: kapanmis task context kopyalari
- `tasks/<task-id>/`: task bazli brief, handoff, artifact ve raporlar

## Kurallar

- Agent'lar sohbet gecmisini degil, atanmis context path'lerini birincil kaynak kabul eder.
- Her agent yalnizca kendi artifact veya report yoluna yazar.
- Baska agent handoff dosyasi dogrudan degistirilmez.
