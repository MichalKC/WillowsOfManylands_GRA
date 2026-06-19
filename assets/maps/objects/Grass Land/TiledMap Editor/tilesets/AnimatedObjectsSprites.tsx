<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.1" name="AnimatedObjectsSprites" tilewidth="192" tileheight="192" tilecount="30" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <properties>
  <property name="animation" value="IDLE"/>
  <property name="animationSpeed" type="float" value="1"/>
  <property name="atlasAsset" value="OBJECTS"/>
 </properties>
 <tile id="0" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="12"/>
  </properties>
  <image source="../../AnimatedObjects/grass_tree1.png" width="128" height="192"/>
  <objectgroup draworder="index" id="2">
   <object id="6" x="48.6364" y="132">
    <polygon points="0.266304,1.04348 -6.09511,9.53755 -5.63636,11.3636 -0.272727,11.0909 -4.98814,15.3241 -4.52569,18.0198 3.67194,17.8538 7.90909,24.3636 13.8814,19.498 16.8814,20.0435 24.4111,23.7273 29.4545,21.4545 27.9091,18 35.17,19.7115 38.7352,19.0158 31.9432,8.40909 32.7273,8.81818 35.0909,9.18182 35.4205,6.82386 30.4091,1.19886"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="1" type="GameObject">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="0"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="speed" type="float" value="0"/>
   <property name="z" type="int" value="0"/>
  </properties>
  <image source="../../AnimatedObjects/trap.png" width="16" height="16"/>
 </tile>
 <tile id="2" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="canBeDamaged" type="bool" value="false"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../AnimatedObjects/training_dummy.png" width="128" height="128"/>
  <objectgroup draworder="index" id="2">
   <object id="5" x="60.8438" y="84.875">
    <polygon points="0,0 -4.90625,4.9375 -4.875,8.03125 11.125,8.0625 11.1563,5.0625 6.09375,-0.0625 2.40306,-4.28685"/>
   </object>
   <object id="6" x="54.9924" y="57.0166" width="18.0297" height="17.9863">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="3" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-23"/>
  </properties>
  <image source="../../AnimatedObjects/anvil.png" width="64" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="18.1818" y="19.7273">
    <polygon points="-0.25,-0.65625 -0.21875,3.21875 3.84375,3.21875 3.875,4.5 2.875,4.875 1.75,6.125 1.75,9.125 26.75,9.25 26.75,6.125 24.75,3.25 28.7187,3.28125 28.75,-0.25 32.5,-1 35.5,-3.5 35.875,-6.625 -12.125,-6.625 -9.125,-3.5 -5.31534,-2.27841 -3.40909,-0.872159"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="4" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../AnimatedObjects/blacksmithsignframe.png" width="32" height="36"/>
 </tile>
 <tile id="5" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-25"/>
  </properties>
  <image source="../../AnimatedObjects/blacksmithwaterdeposit.png" width="64" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="12.9091" y="20.1818" width="38.0909" height="8.45455"/>
  </objectgroup>
 </tile>
 <tile id="6" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="12"/>
  </properties>
  <image source="../../AnimatedObjects/grass_tree2.png" width="128" height="192"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="49.0303" y="131.364">
    <polygon points="0,0 -3.125,6.875 -8,12.875 -7.25,14.625 -2.125,13.375 -3.125,16.5 -5,19.625 -4.5,21.125 1.25,20.75 6.875,17.375 11.75,20.75 12.625,23.75 15.75,23.5 21.25,17.75 31.625,22 33.625,21.25 33.375,16.25 29.625,9.75 34.875,9.75 37.5,9.125 32,5.25 30.125,-0.75"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="7" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="35"/>
   <property name="sortOffsetY" type="float" value="-20"/>
  </properties>
  <image source="../../AnimatedObjects/grass_chest2.png" width="64" height="64"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="17.375" y="39.375" width="32.75" height="8.625"/>
  </objectgroup>
 </tile>
 <tile id="8" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="20"/>
   <property name="sortOffsetY" type="float" value="-15"/>
  </properties>
  <image source="../../AnimatedObjects/shrine1-nograss.png" width="64" height="99"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="21.5455" y="76.8182">
    <polygon points="0.0625,0.125 -3.29545,2.67614 -5.36364,5.09091 -5.27273,7.81818 -4.48722,8.93324 -2.77273,9.15057 -0.363636,8.54545 2.31225,10.2964 6.11067,11.1265 9.9249,13.3992 14.1937,11.1858 17,10.4545 20.0909,9.09091 23.2727,9.18182 25.1434,7.79596 25.1477,5.85795 23.8693,3.76705 21.5739,1.52841 18.9091,-0.272727 15.6364,-0.909091 11.4318,-1.32955 7.57955,-1.26705 3.19318,-0.872159"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="9" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="-32"/>
  </properties>
  <image source="../../AnimatedObjects/blacksmithdoor.png" width="96" height="96"/>
 </tile>
 <tile id="10" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="-20"/>
  </properties>
  <image source="../../AnimatedObjects/forge.png" width="64" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="5.09091" y="64.9091">
    <polygon points="2.45455,0.181818 -0.0320491,2.75009 -0.0909091,9.54545 3.63636,15.4545 8.36364,19.9091 15.7273,24.3636 22.0909,26 31.5455,25.9091 35.4545,24.7273 43.3636,21.5455 48.7945,16.7075 52.3874,11.6877 52.4545,7.45455 52.5455,1.27273 50.4545,-1.09091 45.3636,-2.81818 35.5455,-3.81818 24.3636,-4.27273 13.1818,-3.63636 6.81818,-2.54545"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="11" type="StaticProp">
  <properties>
   <property name="activatedAnimations" type="int" value="3"/>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1.5"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="20"/>
  </properties>
  <image source="../../AnimatedObjects/waterwell.png" width="128" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="35.4545" y="82.5455">
    <polygon points="0.173913,-0.173913 -1.3125,3.30114 -2.36364,8 -2,21.2727 0.0197628,27.419 5.45455,34 11.2727,38.7273 19.2727,42.3636 25.8182,44 34,44.2727 42.1818,42 50.5455,37.6364 56.3636,32.3636 60.3636,25.8182 62.3636,19.0909 62.7898,9.63636 60.414,1.51729 55.2443,-6.61364 48.4214,-13.3691 42.0114,-17.8295 30.0341,-19.4602 20.2443,-18.2159 14,-14.7273 6.85795,-8.94318 2.18182,-3.81818"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="12" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="43"/>
  </properties>
  <image source="../../AnimatedObjects/campfire3.png" width="128" height="128"/>
 </tile>
 <tile id="13" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="../../AnimatedObjects/nature-sparkles.png" width="90" height="96"/>
 </tile>
 <tile id="14" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="z" type="int" value="0"/>
  </properties>
  <image source="../../AnimatedObjects/grass1.png" width="16" height="16"/>
 </tile>
 <tile id="15" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="z" type="int" value="0"/>
  </properties>
  <image source="../../AnimatedObjects/grass2.png" width="16" height="16"/>
 </tile>
 <tile id="16" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="z" type="int" value="0"/>
  </properties>
  <image source="../../AnimatedObjects/grass3.png" width="16" height="16"/>
 </tile>
 <tile id="17" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="30"/>
   <property name="storage" type="bool" value="true"/>
   <property name="storageCols" type="int" value="2"/>
   <property name="storageRows" type="int" value="3"/>
   <property name="storageTitle" value="STORAGE"/>
  </properties>
  <image source="../../../Houses/AnimatedObjects/house_chest4.png" width="192" height="192"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="74.9922" y="128">
    <polygon points="0,0 42.0078,-0.00390625 42.0117,-11.0039 0.00390625,-11"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="18" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="../../AnimatedObjects/attention.png" width="33" height="28"/>
 </tile>
 <tile id="19" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/butterfly1Around.png" width="150" height="106"/>
 </tile>
 <tile id="20" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/butterfly2Around.png" width="150" height="106"/>
 </tile>
 <tile id="21" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/butterfly3Around.png" width="150" height="106"/>
 </tile>
 <tile id="22" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/diamondSparkles.png" width="96" height="96"/>
 </tile>
 <tile id="23" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/flies.png" width="96" height="96"/>
 </tile>
 <tile id="24" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-24"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/lamp1.png" width="64" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="44.4375" y="82.875">
    <polygon points="-0.375,-0.1875 -0.65625,2.4375 0.4375,4.8125 4.375,5.15625 8.21875,4.9375 9.5,2.71875 9.25,0.9375 8.46875,-0.34375 6.53125,-0.9375 3.75,-1.125 1.25,-0.96875"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="25" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-24"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/lamp2.png" width="64" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="43.9375" y="87.5625">
    <polygon points="0,0 3.75,1.9375 6.875,1.9375 9.6875,1.375 10.125,-1.5 8.75,-3.1875 7.4375,-4.125 4.875,-4.1875 2,-3.75 0.4375,-2.9375 -0.125,-0.875"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="26" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-24"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/lamp3.png" width="64" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="48.1875" y="78.9375">
    <polygon points="-0.8125,0.5 -2.9375,2.0625 -4.5,4.5 -4,7 -1.6875,9.5 5.25,10.25 11.6875,9.875 13.8125,8 14.125,4.1875 12.75,2.40625 10.8438,0.34375 9.28125,-0.84375 5.125,-1.3125 1.09375,-1.03125"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="27" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-24"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/lamp4.png" width="64" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="36" y="82.625">
    <polygon points="0,0 0.6875,2.9375 3.6875,4.375 7,4.9375 11.6875,4.0625 13.6875,2.25 14.0625,0.0625 12.75,-2.625 9.625,-3.6875 5.9375,-3.8125 2.25,-3 0.625,-1.625"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="28" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="-24"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/lamp5.png" width="64" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="25.4545" y="77.9091">
    <polygon points="0,0 -2.17391,0.426877 -2.75889,4.72332 -0.0909091,5.54545 6.36364,4.63636 8.27273,5.09091 8.81818,7.72727 10.7273,11.2727 14.0909,13.4545 18.5455,12.3636 21.0909,7.54545 22.6364,4.09091 25.2727,5.27273 31.9091,6.27273 33.5341,4.47159 33.4773,0.1875 28.9347,-0.46875 26,-4.54545 24.0909,-6 22.9091,-7.45455 21.8182,-9.09091 20.6364,-11.2727 19.1818,-13.1818 15.6364,-13.7273 12.3636,-13.4545 9.54545,-12.5455 8.27273,-10.7273 7.72727,-7.81818 5.54545,-4.72727 2.90909,-2.18182"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="29" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="sortOffsetY" type="float" value="0"/>
  </properties>
  <image source="../../../Grass Land 2/AnimatedObjects/mosquito1Around.png" width="150" height="106"/>
 </tile>
</tileset>
