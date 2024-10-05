Zum Zugriff auf die OSM Basiskarten der MapTiler Cloud wird ein Access Key benoetigt.  
Fuegen Sie ihren persoenlichen Access Key in der Datei mapTilerKey.ts mit folgender Anweisung hinzu:  
`export default "Your-OpenMapTiler-Key";`   

Zur Anzeige einer Basiskarte aus der MapTiler Cloud muss eine Datei mapTileMapId.ts mit folgender Anweisung hinzugefuegt werden:   
`export default "Your-OpenMapTiler-MapId";`

Zur Installation der Dependencies fuehren Sie folgende Anweisung in der Kommandozeile aus:
```bash
npm install
```

Um einen Entwicklungsserver zur Anzeige des Kartenclients im Browser zu starten, 
fuehren Sie nachstehenden Befehl in einer Kommandozeile aus:
```bash
npm run start
```




