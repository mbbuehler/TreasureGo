TreasureGo
=========
TreasureGo is a game similar to PokémonGO. Instead of searching for Pokémons, we search for Treasures.

Once you get close than 10 meters to a Treasure, an augmented reality (AR) view pops up. You can then collect Gems by clicking on them.

This repository contains the source code of TreasureGo Android.

Have fun playing TreasureGo!


Please Note
=========
- You can place a CSV file similar to the default file (app/src/main/res/raw/treasures.csv) into the root of your external storage directory. If the app finds a file called "teasures.csv" at the path returned by "getExternalStorageDirectory()", it will load that file. If there is no such file available it will load the default file with some Treasure located on Hönggerberg, Zürich, Switzerland.
- If the arrow does not work well, you can calibrate the sensors of your phone.

Talk to me
=========
I am happy to receive your feedback or advice you in case you are interested to further develop this app or just learn more about it.

Features
=========
Here are some ideas about possible new features:
- Time bonus: In the AR view, let a clock fly by. If the user catches the clock she gets rewarded 5 extra seconds.
- Save game status: Save the game status such that the user can keep playing even after closing the app.
- Multiplayer: Allow multiple players to search for the same Treasure.
- Score board for multiplayer


Android SDK Version
=========
Target SDK Version: 27


Screenshots
=========
## Main View
![main](https://user-images.githubusercontent.com/9430720/39466708-9160a868-4d2a-11e8-9bc6-0eceb531508d.png)
## Compass View
![compass](https://user-images.githubusercontent.com/9430720/39466721-a2b13d44-4d2a-11e8-8518-aa4f6f1f7384.png)
## Augmented Reality View
![fireopal](https://user-images.githubusercontent.com/9430720/39466728-a7a9a70a-4d2a-11e8-98e5-567d8eb1c95b.png)
## Map View
![maps](https://user-images.githubusercontent.com/9430720/39466731-a9a20110-4d2a-11e8-80d4-67c1a1620f58.png)
## Treasure Found View
![treasure-found](https://user-images.githubusercontent.com/9430720/39466732-abbfda94-4d2a-11e8-968c-e674630f2e31.png)
