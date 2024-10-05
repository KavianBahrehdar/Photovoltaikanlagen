# Geoinformatik2

# Bahrehdar, Kavian, 22102523 
# Yazdekhasti, Shakiba, 22102528

## Erste Schtitte

um das Projekt nach dem Klonen starten zu können, müssen Sie auf map-client-template navigieren und mit `npm run start` die ClientServer starten.
Gehen Sie auch auf map-server-template und starten Sie `MapServerApplication.java`.
Das Client läuft auf Port 4711 und Server auf 8080.

Wenn Sie auf **localhost:8080** gehen, sehen Sie das LandingPage mit allen definierten Endpunkten.

**Stellen Sie sicher das Client unbedingt auf localhost:4711 started, sonst berechen Sie alle andere läufende Anwendungen auf diesem Port ab, damit diese Client Server auf Port 4711 starten kann**

## Code Erkärung
das Java Projekt auf PostgreSQL Verzeichnis ist dazu zuständig, die Geojson Dateien auszulesen und in Datenbank hochzuladen. (Sehen Sie für diese Implementierung dieses unten stehendes Verzeichnis: PostgreSQL -> PostGis -> src -> test -> PostgresqlPoiRepositoryTest.java)

Das Java Projekt auf map-server-template ist dazu zuständig, die Server zu starten. (Sehen Sie für die Implementierung das FeatureCollectionsController.java)

Für Die Implementierung des Client-Seite navigieren Sie dann auf map-client-template und sehen Sie intex.ts und intex.html