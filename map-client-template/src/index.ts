import maplibre from "maplibre-gl";
import mapTilerKey from "./mapTilerKey";
import axios from "axios"
import MapboxDraw from "@mapbox/mapbox-gl-draw";
const wrongCoordinates = [
    [ 13.13551, 48.58538],
    [ 13.175029, 48.597819],
    [ 13.175176, 48.598453],
    [ 13.175197, 48.598276],
    [ 13.175283, 48.597758],
    [ 13.174695, 48.598416],
    [ 13.175106, 48.597113],
    [ 13.174761, 48.597641],
    [ 13.175284, 48.597708],
    [ 13.174632, 48.597645],
    [ 13.175129, 48.598236],
    [ 13.1726, 48.62554],
    [ 13.11965, 48.577902],
    [ 13.119579, 48.577829],
    [ 13.156782, 48.599569],
    [ 13.156729, 48.599694],
    [ 13.157087, 48.599724],
    [ 13.155697, 48.600154],
    [ 13.133, 48.58486],
    [ 13.178805, 48.891262],
    [ 13.165154, 48.892127],
    [ 13.142849, 48.923542],
    [ 13.246907, 48.876726],
    [ 12.818002, 48.892018],
    [ 12.79639, 48.91226],
    [ 12.815694, 48.895186],
    [ 12.835383, 48.939368],
    [ 12.544947, 48.414775],
    [ 12.544947, 48.414775],
    [ 12.0, 48.0],
    [ 12.667413, 48.748604],
    [ 13.0, 48.0],
    [ 13.04496, 48.465846],
    [ 12.891221, 48.415853],
    [ 12.5628, 48.4417],
    [ 13.0, 48.0],
    [ 12.181743, 48.907127],
    [ 11.957411, 48.495419],
    [ 11.554363, 48.373176],
    [ 10.562937, 51.098487],
    [ 10.587794, 51.103436],
    [ 12.861446, 48.909108],
    [ 12.86013, 48.910625]

]
var basemaps ={
    "basic": `https://api.maptiler.com/maps/basic-v2/style.json?key=${mapTilerKey}`,
    "satellite": `https://api.maptiler.com/maps/satellite/style.json?key=${mapTilerKey}`,
    "streets": `https://api.maptiler.com/maps/streets-v2/style.json?key=${mapTilerKey}`,
    "openstreetmap": `https://api.maptiler.com/maps/openstreetmap/style.json?key=${mapTilerKey}`
}
const map = new maplibre.Map({
    container: "map",
    center: [12.954957, 48.829502], // map center location [lng, lat]
    zoom: 9, // map zoom
    style: `https://api.maptiler.com/maps/basic-v2/style.json?key=${mapTilerKey}`,
    attributionControl: false
}).addControl(
    new maplibre.AttributionControl({
        compact: true // reduces the copyright attributions view
    })
);
const scale = new maplibre.ScaleControl({
    maxWidth: 80, // The maximum length of the scale control in pixels
    unit: "metric" // Unit of the scale (metric, imperial, or nautical)
});
map.addControl(scale);


map.addControl(new maplibre.NavigationControl());
map.addControl(new maplibre.FullscreenControl());
map.addControl(new maplibre.GeolocateControl({
    positionOptions: {
        enableHighAccuracy: true
    },
    trackUserLocation: true
}));
(MapboxDraw as any).constants.classes.CONTROL_BASE  = 'maplibregl-ctrl';
(MapboxDraw as any).constants.classes.CONTROL_PREFIX = 'maplibregl-ctrl-';
(MapboxDraw as any).constants.classes.CONTROL_GROUP = 'maplibregl-ctrl-group';
const draw = new MapboxDraw({
    displayControlsDefault: false,
    controls: {
        polygon: true,
        trash: true
    }
});
(map as any).addControl(draw)
map.on('draw.create', updatePolygon);
map.on('draw.update', updatePolygon);
map.on('draw.delete', deletePolygon);

function deletePolygon(e){
    map.removeLayer("solar-boundingbox")
    map.removeSource("solar-boundingbox")
}
function updatePolygon(e) {
    const data = draw.getAll();
    if (data.features.length > 0) {
        const polygon = data.features[0].geometry as GeoJSON.Polygon;
        const coordinates = polygon.coordinates[0];
        console.log('Polygon Coordinates:', coordinates);
        sendPolygonToBackend(coordinates);
    }
}
async function sendPolygonToBackend(coordinates) {
    console.log('Polygon Coordinates:', coordinates);
    try {
        const response = await axios.post("http://localhost:8080/intersection", {
            coordinates: coordinates
        }, {
            headers: {
                "Content-Type": "application/json"
            }
        });
        const data = await response.data;
        // console.log(data)
        const geoJsonData: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
            type: "FeatureCollection",
            features: data.map((feature: any) => ({
                type: "Feature",
                properties: feature.properties,
                geometry: {
                    type: "Point",
                    coordinates: [feature.geometry.x, feature.geometry.y]
                }
            }))
        };

        
        if (!map.getLayer("solar-boundingbox")) {
            map.addLayer({
                id: "solar-boundingbox",
                type: "circle",
                source: {
                    type: "geojson",
                    data: geoJsonData
                },
                paint: {
                    "circle-radius": 5,
                    "circle-color": "blue"
                }
            });
            map.on("click", "solar-boundingbox", function (e) {
                const coordinates = (e.features[0].geometry as GeoJSON.Point).coordinates.slice();
                const properties = e.features[0].properties;

                new maplibre.Popup()
                    .setLngLat(coordinates  as [number, number])
                    .setHTML(`<h3>${properties.Gemeinde}</h3><p>Energie: ${properties.Energietra}</p>`)
                    .addTo(map);
            });
            map.on("mouseenter", "solar-boundingbox", function () {
                map.getCanvas().style.cursor = "pointer";
            });

            map.on("mouseleave", "solar-boundingbox", function () {
                map.getCanvas().style.cursor = "";
            });
        } else {
            
            (map.getSource("solar-boundingbox") as maplibre.GeoJSONSource).setData(geoJsonData);
        }

    } catch (error) {
        console.error("Fehler beim Abrufen der Daten:", error);
    }
}

async function loadPointsForLocation(location: string) {
    try {
        const response = await fetch(`http://localhost:8080/features/${encodeURIComponent(location)}`);
        if (!response.ok) {
            throw new Error("Netzwerkantwort war nicht ok " + response.statusText);
        }
        const data = await response.json();

        const geoJsonData: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
            type: "FeatureCollection",
            features: data.map((feature: any) => ({
                type: "Feature",
                properties: feature.properties,
                geometry: {
                    type: "Point",
                    coordinates: [feature.geometry.x, feature.geometry.y]
                }
            }))
        };

        const coordinates = geoJsonData.features.map(feature => feature.geometry.coordinates as [number, number]);

        if (!map.getLayer("solar")) {
            map.addLayer({
                id: "solar",
                type: "circle",
                source: {
                    type: "geojson",
                    data: geoJsonData
                },
                paint: {
                    "circle-radius": [
                        "interpolate",
                        ["linear"],
                        ["get", "someProperty"], // Passe "someProperty" an die tatsächliche Eigenschaft in deinen Daten an
                        1, 5,
                        10, 10
                    ],
                    "circle-color": [
                        "interpolate",
                        ["linear"],
                        ["get", "someProperty"], // Passe "someProperty" an die tatsächliche Eigenschaft in deinen Daten an
                        1, "blue",
                        10, "red"
                    ]
                }
            });
            map.on("click", "solar", function (e) {
                const coordinates = (e.features[0].geometry as GeoJSON.Point).coordinates.slice();
                const properties = e.features[0].properties;

                new maplibre.Popup()
                    .setLngLat(coordinates  as [number, number])
                    .setHTML(`<h3>${properties.Gemeinde}</h3><p>${properties.Laengengra}</p>`)
                    .addTo(map);
            });
            map.on("mouseenter", "solar-all", function () {
                map.getCanvas().style.cursor = "pointer";
            });

            map.on("mouseleave", "solar-all", function () {
                map.getCanvas().style.cursor = "";
            });
        } else {
            
            (map.getSource("solar") as maplibregl.GeoJSONSource).setData(geoJsonData);
        }

        if (coordinates.length > 0) {
            const bounds = coordinates.reduce(
                (bounds, coord) => bounds.extend(coord),
                new maplibre.LngLatBounds(coordinates[0], coordinates[0])
            );
            map.fitBounds(bounds, { padding: 20 });
        }
    } catch (error) {
        console.error("Fehler beim Abrufen der Daten:", error);
    }
}
////////////////////////////////// change opacity functions
function changeLayerOpacity(layerId, layerTyp ,opacity) {
    if (map.getLayer(layerId)) {
        map.setPaintProperty(layerId, layerTyp, opacity);
    } else {
        console.error("Layer does not exist:", layerId);
    }
}
////////////////////////////////////////////// change color functions

function changeLayerColor(layerId ,layerTyp, color){
    map.setPaintProperty(layerId, layerTyp, color)
}
/////////////////////////////////////////////
async function addCluster() {
    try {
        const response = await fetch("http://localhost:8080/features");
        if (!response.ok){
            throw new Error("Netzwerk war nicht ok" + response.statusText)
        }
        const data = await response.json();
        const geojsonData: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
            type: "FeatureCollection",
            features: data.map((feature: any) => ({
                type: "Feature",
                properties: feature.properties,
                geometry: {
                    type: "Point",
                    coordinates: [feature.geometry.x, feature.geometry.y]
                }
            }))
        };
        map.addSource("points", {
            type: "geojson",
            data: geojsonData,
            cluster: true,
            clusterMaxZoom: 14,
            clusterRadius: 50 
        });
    
        map.addLayer({
            id: "clusters",
            type: "circle",
            source: "points",
            filter: ["has", "point_count"],
            paint: {
                // Use step expressions to implement three types of circles:
                // * Blue, 20px circles when point count is less than 100
                // * Yellow, 30px circles when point count is between 100 and 750
                // * Pink, 40px circles when point count is greater than or equal to 750
                "circle-color": [
                    "step",
                    ["get", "point_count"],
                    "#51bbd6",
                    100,
                    "#f1f075",
                    750,
                    "#f28cb1"
                ],
                "circle-radius": [
                    "step",
                    ["get", "point_count"],
                    20,
                    50,
                    30,
                    70,
                    40,
                    100,
                    50
                ]
            }
        });
    
        map.addLayer({
            id: "cluster-count",
            type: "symbol",
            source: "points",
            filter: ["has", "point_count"],
            layout: {
                "text-field": "{point_count_abbreviated}",
                "text-font": ["DIN Offc Pro Medium", "Arial Unicode MS Bold"],
                "text-size": 12
            }
        });
    

    } catch (error) {
        console.error("Fehler beim Abrufen der Daten:", error);
    }
    
}
async function loadWrongPoints() {
    const wrongPointsGeoJson: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
        type: "FeatureCollection",
        features: wrongCoordinates.map(coord => ({
            type: "Feature",
            properties: {},
            geometry: {
                type: "Point",
                coordinates: coord
            }
        }))
    };

    if (!map.getLayer("wrong-points")) {
        map.addLayer({
            id: "wrong-points",
            type: "circle",
            source: {
                type: "geojson",
                data: wrongPointsGeoJson
            },
            paint: {
                "circle-radius": 5,
                "circle-color": "red"
            }
        });

        map.on("click", "wrong-points", function (e) {
            const coordinates = (e.features[0].geometry as GeoJSON.Point).coordinates.slice();
            const properties = e.features[0].properties;

            new maplibre.Popup()
                .setLngLat(coordinates as [number, number])
                .setHTML(`<p>coordinates: ${coordinates}</p>`)
                .addTo(map);
        });

        map.on("mouseenter", "wrong-points", function () {
            map.getCanvas().style.cursor = "pointer";
        });

        map.on("mouseleave", "wrong-points", function () {
            map.getCanvas().style.cursor = "";
        });
    } else {
        (map.getSource("wrong-points") as maplibregl.GeoJSONSource).setData(wrongPointsGeoJson);
    }
}
async function loadPoints() {
    try {
        const response = await fetch("http://localhost:8080/features/");
        if (!response.ok) {
            throw new Error("Netzwerkantwort war nicht ok " + response.statusText);
        }
        const data = await response.json();
        // console.log(data)
        const filteredData = data.filter((feature: any) => {
            const coordinates = [feature.geometry.x, feature.geometry.y];
            return !wrongCoordinates.some(wrongCoord => 
                wrongCoord[0] === coordinates[0] && wrongCoord[1] === coordinates[1]
            );
        });

        const geoJsonData: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
            type: "FeatureCollection",
            features: filteredData.map((feature: any) => ({
                type: "Feature",
                properties: feature.properties,
                geometry: {
                    type: "Point",
                    coordinates: [feature.geometry.x, feature.geometry.y]
                }
            }))
        };

        
        if (!map.getLayer("solar-all")) {
            map.addLayer({
                id: "solar-all",
                type: "circle",
                source: {
                    type: "geojson",
                    data: geoJsonData
                },
                paint: {
                    "circle-radius": 5,
                    "circle-color": "blue"
                }
            });
            map.on("click", "solar-all", function (e) {
                const coordinates = (e.features[0].geometry as GeoJSON.Point).coordinates.slice();
                const properties = e.features[0].properties;
    
                new maplibre.Popup()
                    .setLngLat(coordinates  as [number, number])
                    .setHTML(`<h3>${properties.Gemeinde}</h3><p>Energie: ${properties.Energietra}</p><br>coordinates:${coordinates}`)
                    .addTo(map);
            });
            map.on("mouseenter", "solar-all", function () {
                map.getCanvas().style.cursor = "pointer";
            });

            map.on("mouseleave", "solar-all", function () {
                map.getCanvas().style.cursor = "";
            });
        } else {
            
            (map.getSource("solar-all") as maplibregl.GeoJSONSource).setData(geoJsonData);
        }

    } catch (error) {
        console.error("Fehler beim Abrufen der Daten:", error);
    }
}

async function addPolygonToMap() {
    try {
        const response = await fetch("http://localhost:8080/polygon");
        if (!response.ok) {
            throw new Error("Network response was not ok: " + response.statusText);
        }
        const data = await response.json();
        
        const coordinates = data[0].geometry.vertices.map(vertex => [vertex.x, vertex.y]);

        map.addLayer({
            id: "polygon-layer-deggendorf",
            type: "fill",
            source: {
                type: "geojson",
                data: {
                    type: "Feature",
                    geometry: {
                        type: "Polygon",
                        coordinates: [coordinates]
                    },
                    properties: {
                    }
                }
            },
            layout: {},
            paint: {
                "fill-color": "#088",
                "fill-opacity": 0.4
            }
        });

        const bounds = coordinates.reduce((bounds, coord) => bounds.extend(coord), new maplibre.LngLatBounds(coordinates[0], coordinates[0]));
        map.fitBounds(bounds, { padding: 20 });

    } catch (error) {
        console.error("Error fetching or displaying polygon:", error);
    }
}
const hexagonDeggendorfId=[]
const hexagonCountDeggendorfId=[]
async function addHexagonToMap() {
    try {
        const response = await fetch("http://localhost:8080/hexagon");
        if (!response.ok) {
            throw new Error("Network response was not ok: " + response.statusText);
        }
        const data = await response.json();

        data.forEach(hexagon => {
            const coordinates = hexagon.geometry.vertices.map(vertex => [vertex.x, vertex.y]);

            hexagonDeggendorfId.push(`hexagon-layer-${hexagon.id}`)
            hexagonCountDeggendorfId.push(`hexagon-count-layer-${hexagon.id}`)

            if (hexagon.properties.count > 0) {
                map.addLayer({
                    id: `hexagon-layer-${hexagon.id}`,
                    type: "line",
                    source: {
                        type: "geojson",
                        data: {
                            type: "Feature",
                            geometry: {
                                type: "Polygon",
                                coordinates: [coordinates]
                            },
                            properties: {
                                count: hexagon.properties.count 
                            }
                        }
                    },
                    layout: {},
                    paint: {
                        "line-color": "#088",
                        "line-width": 2
                    }
                });
        
                map.addLayer({
                    id: `hexagon-count-layer-${hexagon.id}`,
                    type: "symbol",
                    source: `hexagon-layer-${hexagon.id}`,
                    layout: {
                        "text-field": ["get", "count"],
                        "text-size": 12,
                        "text-transform": "uppercase",
                        "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
                        "text-offset": [0, 0.6],
                        "text-anchor": "top"
                    },
                    paint: {
                        "text-color": "#000"
                    }
                });
            }
            map.on("click", `hexagon-layer-${hexagon.id}`, function (e) {
                new maplibre.Popup()
                    .setLngLat(e.lngLat)
                    .setHTML(`<h3>Count: ${hexagon.properties.count}</h3>`)
                    .addTo(map);
            });
        });

        // const bounds = calculateBounds(data);
        // map.fitBounds(bounds, { padding: 20 });

    } catch (error) {
        console.error("Error fetching or displaying polygons:", error);
    }
}
async function add3DHexagonToMap() {
    map.setPitch(60); 
    // map.setBearing(40);
    try {
        const response = await fetch("http://localhost:8080/hexagon");
        if (!response.ok) {
            throw new Error("Network response was not ok: " + response.statusText);
        }
        const data = await response.json();

        const features = data
            .filter(hexagon => parseInt(hexagon.properties.count) > 0) 
            .map(hexagon => {
            const coordinates = hexagon.geometry.vertices.map(vertex => [vertex.x, vertex.y]);
            return {
                type: "Feature",
                geometry: {
                    type: "Polygon",
                    coordinates: [coordinates]
                },
                properties: {
                    count: parseInt(hexagon.properties.count)
                }
            };
        });

        const geojson: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
            type: "FeatureCollection",
            features: features
        };

        map.addSource("hexagon-layer-3d", {
            type: "geojson",
            data: geojson
        });

        // Add a single layer
        map.addLayer({
            id: "hexagon-layer-3d",
            type: "fill-extrusion",
            source: "hexagon-layer-3d",
            layout: {},
            paint: {
                "fill-extrusion-color": [
                    "interpolate",
                    ["linear"],
                    ["get", "count"], 
                    1, "rgba(255, 255, 255, 0.5)", 
                    10, "rgba(0, 0, 255, 0.5)", 
                    20, "rgba(255, 0, 0, 0.5)" 
                ],
                "fill-extrusion-height": [
                    "interpolate",
                    ["linear"],
                    ["get", "count"],
                    1, 100,  // Minimal height for count 1
                    30, 3000 // Maximal height for count 30
                ],
                "fill-extrusion-base": 0,
                "fill-extrusion-opacity": 1
            }
        });

        map.on("click", "hexagon-layer-3d", function (e) {
            const coordinates = e.lngLat;
            const properties = e.features[0].properties;
            new maplibre.Popup()
                .setLngLat(coordinates)
                .setHTML(`<h3>Count: ${properties.count}</h3>`)
                .addTo(map);
        });

    } catch (error) {
        console.error("Error fetching or displaying polygons:", error);
    }
}
async function loadPointsAndCreateHeatmap() {
    try {
        // Daten von der API abrufen
        const response = await fetch("http://localhost:8080/features");
        if (!response.ok) {
            throw new Error("Network response was not ok: " + response.statusText);
        }
        const data = await response.json();

        // GeoJSON Daten erstellen
        const filteredData = data.filter((feature: any) => {
            const coordinates = [feature.geometry.x, feature.geometry.y];
            return !wrongCoordinates.some(wrongCoord => 
                wrongCoord[0] === coordinates[0] && wrongCoord[1] === coordinates[1]
            );
        });

        const geoJsonData: GeoJSON.FeatureCollection<GeoJSON.Point, any> = {
            type: "FeatureCollection",
            features: filteredData.map((feature: any) => ({
                type: "Feature",
                properties: feature.properties,
                geometry: {
                    type: "Point",
                    coordinates: [feature.geometry.x, feature.geometry.y]
                }
            }))
        };

        // Überprüfen, ob der Heatmap-Layer bereits existiert, andernfalls erstellen
        if (!map.getSource("heatmap-source")) {
            map.addSource("heatmap-source", {
                type: "geojson",
                data: geoJsonData
            });

            map.addLayer({
                id: "heatmap-layer",
                type: "heatmap",
                source: "heatmap-source",
                maxzoom: 15,
                paint: {
                    "heatmap-color": [
                        "interpolate",
                        ["linear"],
                        ["heatmap-density"],
                        0, "rgba(33,102,172,0)",
                        0.1, "rgb(103,169,207)",
                        0.2, "rgb(209,229,240)",
                        0.3, "rgb(253,219,199)",
                        0.4, "rgb(239,138,98)",
                        0.6, "rgb(178,24,43)"
                    ],
                    "heatmap-opacity": {
                        stops: [
                            [11, 1],
                            [15, 0]
                        ]
                    },
                    "heatmap-intensity": {
                        stops: [
                            [11, 0.5],
                            [15, 0.5]
                        ]
                    },
                    "heatmap-radius": {
                        stops: [
                            [11, 15],
                            [15, 20]
                        ]
                    }
                }
            });

        } else {
            (map.getSource("heatmap-source") as maplibregl.GeoJSONSource).setData(geoJsonData);
        }

    } catch (error) {
        console.error("Error fetching or displaying points:", error);
    }
}
document.getElementById("toggle3D").addEventListener("click", function() {
    add3DHexagonToMap();
});

document.getElementById("toggle2D").addEventListener("click", function() {

    map.setPitch(0); 
    map.setBearing(0);
    map.removeLayer("hexagon-layer-3d");
    map.removeSource("hexagon-layer-3d");
});

var currentBasemap = 'basic';
map.setStyle(basemaps[currentBasemap]);

document.getElementById('basic').addEventListener('click', function() {
    map.setStyle(basemaps['basic']);
    currentBasemap = 'basic';
    addCluster();
    addHexagonToMap();
    addPolygonToMap();
    loadPoints();
    // loadPointsForLocation("Deggendorf")
    loadPointsAndCreateHeatmap();
    setTimeout(loadWrongPoints, 4000);

});

document.getElementById('satellite').addEventListener('click', function() {
    map.setStyle(basemaps['satellite']);
    currentBasemap = 'satellite';
    
    addCluster();
    addHexagonToMap();
    addPolygonToMap();
    loadPoints();
    // loadPointsForLocation("Deggendorf")
    loadPointsAndCreateHeatmap();
    setTimeout(loadWrongPoints, 4000);

});

document.getElementById('openstreetmap').addEventListener('click', function() {
    map.setStyle(basemaps['openstreetmap']);
    currentBasemap = 'openstreetmap';
    addCluster();
    addHexagonToMap();
    addPolygonToMap();
    loadPoints();
    // loadPointsForLocation("Deggendorf")
    loadPointsAndCreateHeatmap();
    setTimeout(loadWrongPoints, 4000);

});

document.getElementById('streets').addEventListener('click', function() {
    map.setStyle(basemaps['streets']);
    currentBasemap = 'streets';
    addCluster();
    addHexagonToMap();
    addPolygonToMap();
    loadPoints();
    // loadPointsForLocation("Deggendorf")
    loadPointsAndCreateHeatmap();
    
    setTimeout(loadWrongPoints, 4000);


});
document.addEventListener("DOMContentLoaded", () => {
    const toggleWrong = document.getElementById("toggleLayer0");
    const toggleSolar = document.getElementById("toggleLayer1");
    const toggleSolarAllPoints = document.getElementById("toggleLayer2");
    const toggleDeggendorfPolygon = document.getElementById("toggleLayer3");
    const toggleDeggendorfHexagon = document.getElementById("toggleLayer4");
    const toggleHeatmap = document.getElementById("toggleLayer5");
    const toggleCluster = document.getElementById("toggleLayer6");



    const opacitySlider1 = document.getElementById("opacitySlider1");
    const opacitySlider2 = document.getElementById("opacitySlider2");
    const opacitySlider3 = document.getElementById("opacitySlider3");
    const opacitySlider4 = document.getElementById("opacitySlider4");
    const opacitySlider5 = document.getElementById("opacitySlider5");
    const opacitySlider6 = document.getElementById("opacitySlider6");


    
    const colorPicker1 = document.getElementById("colorPicker1");
    const colorPicker2 = document.getElementById("colorPicker2");
    const colorPicker3 = document.getElementById("colorPicker3");
    const colorPicker4 = document.getElementById("colorPicker4");
    const colorPicker5 = document.getElementById("colorPicker5");
    const colorPicker6 = document.getElementById("colorPicker6");



    const locationSelect = document.getElementById("locationSelect") as HTMLSelectElement;
    
    locationSelect.addEventListener("change", async () => {
        const selectedLocation = locationSelect.value;
        if (selectedLocation) {
            await loadPointsForLocation(selectedLocation);
        }
    });
    opacitySlider1.addEventListener("input", () => {
        const checkedSolar = (toggleSolar as HTMLInputElement).checked;

        if (checkedSolar) {
            changeLayerOpacity("solar", "circle-opacity", parseFloat((opacitySlider1 as HTMLInputElement).value));
        }
    });
    opacitySlider2.addEventListener("input", () => {
        const checkedSolarAllPoints = (toggleSolarAllPoints as HTMLInputElement).checked;

        if (checkedSolarAllPoints) {
            changeLayerOpacity("solar-all", "circle-opacity", parseFloat((opacitySlider2 as HTMLInputElement).value));
        }
    });
    opacitySlider3.addEventListener("input", () => {
        const checkedDeggendorfPolygon =(toggleDeggendorfPolygon as HTMLInputElement).checked;

        if (checkedDeggendorfPolygon) {
            changeLayerOpacity("polygon-layer-deggendorf","fill-opacity", parseFloat((opacitySlider3 as HTMLInputElement).value));
        }
 
    });
    opacitySlider4.addEventListener("input", () => {

        const checkedDeggendorfHexagon =(toggleDeggendorfHexagon as HTMLInputElement).checked;
        if (checkedDeggendorfHexagon) {
            hexagonDeggendorfId.forEach(layerId => {
                changeLayerOpacity(layerId, "line-opacity", parseFloat((opacitySlider4 as HTMLInputElement).value));
            })
            hexagonCountDeggendorfId.forEach(layerId => {
                changeLayerOpacity(layerId, "text-opacity", parseFloat((opacitySlider4 as HTMLInputElement).value));
            })
        }
    });
    opacitySlider5.addEventListener("input", () => {
        const checkedHeatMap =(toggleHeatmap as HTMLInputElement).checked;

        if (checkedHeatMap) {
            changeLayerOpacity("heatmap-layer","heatmap-opacity", parseFloat((opacitySlider5 as HTMLInputElement).value));
        }
 
    });
    opacitySlider6.addEventListener("input", () => {
        const checkedCluster =(toggleCluster as HTMLInputElement).checked;

        if (checkedCluster) {
            changeLayerOpacity("clusters","circle-opacity", parseFloat((opacitySlider6 as HTMLInputElement).value));
            changeLayerOpacity("cluster-count","text-opacity", parseFloat((opacitySlider6 as HTMLInputElement).value));
        }
 
    });

    toggleSolar.addEventListener("change", () => {
        const checked = (toggleSolar as HTMLInputElement).checked;
        changeLayerOpacity("solar", "circle-opacity", checked ? parseFloat((opacitySlider1 as HTMLInputElement).value) : 0);
    });

   
    toggleSolarAllPoints.addEventListener("change", () => {
        const checked = (toggleSolarAllPoints as HTMLInputElement).checked;
        changeLayerOpacity("solar-all", "circle-opacity", checked ? parseFloat((opacitySlider2 as HTMLInputElement).value) : 0);
    });
    toggleDeggendorfPolygon.addEventListener("change", () => {
        const checked = (toggleDeggendorfPolygon as HTMLInputElement).checked;
        changeLayerOpacity("polygon-layer-deggendorf","fill-opacity", checked ? parseFloat((opacitySlider3 as HTMLInputElement).value) : 0);
    });
    toggleDeggendorfHexagon.addEventListener("change", () => {
        const checked = (toggleDeggendorfHexagon as HTMLInputElement).checked;
        const opacity = checked ? parseFloat((opacitySlider4 as HTMLInputElement).value) : 0;
        
        hexagonDeggendorfId.forEach(layerId => {
            changeLayerOpacity(layerId, "line-opacity", opacity);
        })
        hexagonCountDeggendorfId.forEach(layerId => {
            changeLayerOpacity(layerId, "text-opacity", opacity);
        })
    });
    toggleHeatmap.addEventListener("change", () => {
        const checked = (toggleHeatmap as HTMLInputElement).checked;
        changeLayerOpacity("heatmap-layer","heatmap-opacity", checked ? parseFloat((opacitySlider5 as HTMLInputElement).value) : 0);
    });
    toggleHeatmap.addEventListener("change", () => {
        const checked = (toggleHeatmap as HTMLInputElement).checked;
        changeLayerOpacity("heatmap-layer","heatmap-opacity", checked ? parseFloat((opacitySlider5 as HTMLInputElement).value) : 0);
    });
    toggleCluster.addEventListener("change", () => {
        const checked = (toggleCluster as HTMLInputElement).checked;
        changeLayerOpacity("clusters","circle-opacity", checked ? parseFloat((opacitySlider6 as HTMLInputElement).value) : 0);
        changeLayerOpacity("cluster-count","text-opacity", checked ? parseFloat((opacitySlider6 as HTMLInputElement).value) : 0);
    });
    toggleWrong.addEventListener("change", () => {
        const checked = (toggleWrong as HTMLInputElement).checked;
        changeLayerOpacity("wrong-points","circle-opacity", checked ? 1.0 : 0);
    });



    colorPicker1.addEventListener("input", ()=>{
        const color = (colorPicker1 as HTMLInputElement).value;
        changeLayerColor("solar", "circle-color", color)
    })
    colorPicker2.addEventListener("input", ()=>{
        const color = (colorPicker2 as HTMLInputElement).value;
        changeLayerColor("solar-all", "circle-color", color)
    })
    colorPicker3.addEventListener("input", ()=>{
        const color = (colorPicker3 as HTMLInputElement).value;
        changeLayerColor("polygon-layer-deggendorf", "fill-color", color)
    })
    colorPicker4.addEventListener("input", ()=>{
        const color = (colorPicker4 as HTMLInputElement).value;
        hexagonDeggendorfId.forEach(layerId => {
            changeLayerColor(layerId, "line-color", color);
        })
        hexagonCountDeggendorfId.forEach(layerId => {
            changeLayerColor(layerId, "text-color", color);
        })
    })
    // colorPicker5.addEventListener("input", ()=>{
    //     const color = (colorPicker5 as HTMLInputElement).value;
    //     changeLayerColor("heatmap-layer", "heatmap-color", color)
    // })
    colorPicker6.addEventListener("input", ()=>{
        const color = (colorPicker6 as HTMLInputElement).value;
        changeLayerColor("clusters", "circle-color", color)
    })
});

map.on("load", function() {
    // addCircle();
    addCluster();
    addHexagonToMap();
    addPolygonToMap();
    loadPoints();
    // loadPointsForLocation("Deggendorf")
    loadPointsAndCreateHeatmap();
    loadWrongPoints();

    const targets = {
        "solar-all":"Photovoltaikanlagen im Landkreis Deggendorf",
        "heatmap-layer": "Heatmap",
        "clusters": "Clusters",
        "cluster-count": "Number of Photovoltaikanlagen in Clusters",
        "hexagons": "Hexagons",
        "polygon-layer-deggendorf": "Deggendorf"
    };

    const options = {
        showDefault: true,
        onlyRendered: false,
        showCheckbox: false,
        reverseOrder: false
    };

    // map.addControl(new MaplibreLegendControl.MaplibreLegendControl(targets, options), "bottom-right");
    
});
