# FitnessGo
A fitness application based on Pokemon Go

## Bemutatás
Az alkalmazás egy fitnesz program, melyhez az ötletet a Pokemon Go nevű játék adta. Ennek az alkalmazásnak szintén van egy olyan játékmódja, melyben a felhasználónak a valós tér különböző helyszíneire kell elmennie, hogy pontokat szerezzen, azonban itt a kritikus tényező az, hogy mennyire gyorsan jut el a helyszínre. Ezáltal a játékos ösztönözve van a futásra, így a sportolás játékká válik számára. A fő célközönség olyan aktívkorúak, akik nehezen veszik rá magukat a sportra, azonban nosztalgikusan gondolnak vissza a Pokemon játékokkal eltöltött gyerekkorukra.

## Főbb funkciók
Az alkalmazásban található egy GridView alapú főmenü, melyekből az alfunkciók indíthatók. Balról előhúzható egy drawer menü, melyben a belépett felhasználó információi láthatók, valamint a kijelentkezés lehetősége. Az alkalmazás egy login activityvel indít, melyből elérhető a regisztrációs activity is. A felhasználókezelés emailcím és jelszó alapján történik a Firebase Auth segítségével.

Ezeken túljutva találja magát a felhasználó a főmenüben. A legfontosabb választható menüpont maga a játék, melyben a képernyő egy részén egy Google Maps MapView segítségével látható a felhasználó aktuális környezete, melyet a GPS koordináták alapján talál meg az alkalmazás. A játék a felhasználó környezetéből egy véletlenszerű pontot választ a térképen, a felhasználó feladata pedig ennek a pontnak a 100 méteres sugarán belül kerülni minél hamarabb. Minél kevesebb idő telik el a feladat kiosztása és a teljesítése között, annál több ponttal van jutalmazva a felhasználó.

A célpont megközelítését az alkalmazás egy PendingIntent és egy BroadcastReceiver segítségével figyeli, de a felhasználónak is lehetősége van kérni a pontok jóváhagyását, amennyiben a célpontot legalább 100 méterre megközelítette. Az összegyűjtött pontszámot a felhasználó saját Firestore dokumentumában tárolja a rendszer, melybe automatikusan mentésre kerülnek az adatok, és a játék indításakor a betöltés is magától végbemegy.

A felhasználó érdekében a véletlenül kisorsolt célkoordinátát az alkalmazás először reverse geocodingolja, azaz megkeresi a hozzá tartozó legközelebbi érvényes címet, és ebből geocoding segítségével visszafejt egy olyan koordinátát, melyet a felhasználó közúton már elérhet. Ez a koordináta megjelenítésre kerül a térképen, valamint a hozzá tartozó címet is kiírja a játék a felhasználó addigi pontszáma alatt.

Egy másik, a főmenüből elérhető funkció a Leaderboard, azaz a ranglista, ahol az összes felhasználó közül a legjobbak neve és pontszáma jelenik meg, csökkenő sorrendben. Ez RecyclerView segítségével van megvalósítva.

A harmadik, főmenüből elérhető funkció pedig a profilbeállítások, ahol a felhasználó kiválaszthatja azt a nevet, melyet a játék során használ, a drawer menüben is lát, valamint ez jelenik meg a többi felhasználó számára a ranglistán is a játékos elért pontszáma mellett.

## Felhasznált technológiák
- Explicit Intent segítségével történő váltás a belépő, a regisztrációs és főmenü Activityk között
- A főmenü CardView segítségével került összeállításra
- Az alkalmazás a bejelentkező és a regisztrációs Activityn kívül egyetlen home Activityt használ, a különböző képernyők egy-egy Fragmentként jelennek meg
- A felhasználó email-cím és jelszó segítségével kerül azonosításra Firebase Auth-on keresztül, alkalmazáson belüli regisztrációval
- A felhasználók adatai (emailcím, név, pontszám) Firestore felhőben vannak tárolva
- Az alkalmazásban található egy balról előhúzható Navigation Drawer a profil-információkhoz és tevékenységekhez
- Az alkalmazásban található egy Google térkép MapView, melyen a felhasználó számára megjelenítésre kerül az aktuális pozíciója, valamint az úticél
- A célpont meghatározásához Geocoding és Reverse geocoding API használata
- Az alkalmazás internetkapcsolatot használ a Maps és a Geocoding API működéséhez
- A GPS koordinátákhoz való hozzáféréshez futási idejű engedélykérés a felhasználótól
- Fused Location API használata helymeghatározásra
- Az alkalmazás a felhasználó aktuális pozícióját figyelve location alapú PendingIntent-tel figyeli, hogy eléggé megközelítette-e a célterületet
- A célterület megközelítésénél az alkalmazás BroadcastReceiver komponens segítségével kezeli le a PendingIntentet
- Az eredménylista megjelenítése RecyclerView segítségével történik
- NoboButton nevű 3rd party library segítségével elkészített szimbolikus grafikus gombok
- Az aktuálisan következő célpont meghatározásához az alkalmazás a felhasználó pozíciójához képest egy adott távolságon belül kisorsol egy adott koordinátát, majd azt előbb Reverse Geocoding-olja, hogy megkapja a hozzá tartozó legközelebbi postacímet, majd pedig a postacímet Geocoding-olja, hogy a ténylegesen létező postacímhez tartozó koordináta legyen a célpont, ne pedig egy megközelíthetetlen terület (pl. Duna)
- A pontszámításhoz olyan algoritmust használ, mely végén a pontok egyenesen arányosak a távolsággal és lineárisan csökkennek a kör közben eltelt idővel
