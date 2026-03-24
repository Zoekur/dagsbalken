# Arkitektur: Plattform och produkter

Dagsbalken är uppbyggd som en modulär plattform med separata produkter ovanpå en gemensam kärna.

Plattformen består av tre huvudsakliga lager:

## Core
Innehåller domänlogik kopplad till tid, aktiviteter och scheman. Här definieras hur dagen struktureras och hur olika typer av aktiviteter representeras.

## UI
Innehåller gemensamma visuella komponenter, tema och återanvändbara gränssnittselement. Detta lager möjliggör en konsekvent visuell representation av tid.

## Products
Består av separata applikationer för olika användningskontexter:

- Dagsbalken Home
- Dagsbalken School

Dessa produkter delar samma kärnlogik men skiljer sig åt i funktionalitet, gränssnitt och användningssituation.

Genom att separera plattform och produkter möjliggörs anpassning till olika kontexter utan att duplicera logik. Samtidigt behålls en gemensam grund för hur tid representeras.

Denna struktur ligger i linje med ett tjänsteperspektiv, där värde uppstår i användning och där olika aktörer integrerar samma resurs på olika sätt.
