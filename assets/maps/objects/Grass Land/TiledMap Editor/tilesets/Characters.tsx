<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.1" name="Characters" tilewidth="192" tileheight="160" tilecount="18" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1.5"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="attack2Damage" type="float" value="9"/>
   <property name="attack2DamageDelay" type="float" value="0.4"/>
   <property name="attack2Duration" type="float" value="1.2"/>
   <property name="attackDuration" type="float" value="0.8"/>
   <property name="attackSound" value="SWING"/>
   <property name="damage" type="float" value="7"/>
   <property name="damageDelay" type="float" value="0.2"/>
   <property name="graphicFlipOffsetX" type="float" value="-5"/>
   <property name="life" type="int" value="16"/>
   <property name="lifeReg" type="float" value="0.25"/>
   <property name="sortOffsetY" type="float" value="-12"/>
   <property name="specialDamage" type="float" value="12"/>
   <property name="specialDamageDelay" type="float" value="0.3"/>
   <property name="specialDuration" type="float" value="1.7"/>
   <property name="speed" type="float" value="3"/>
  </properties>
  <image source="../../Characters/warrior.png" width="144" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="60.875" y="66.9432" width="18.6705" height="6.79666">
    <properties>
     <property name="collect" type="bool" value="true"/>
    </properties>
    <ellipse/>
   </object>
   <object id="2" name="attack_sensor_right" x="84.303" y="40.1838" width="22.6667" height="35.6657">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="3" name="attack_sensor_left" x="36.625" y="40.1014" width="22" height="35.0653">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="4" name="attack2_sensor_left" x="2.69479" y="49.1778" width="33.6987" height="5.76805">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="5" name="attack2_sensor_right" x="107.195" y="48.9849" width="33.819" height="6.03633">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="6" name="heart" x="70.1875" y="52.3125" width="2.5" height="2.5">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="7" name="warning" x="71.125" y="12.75" width="3.125" height="2.625">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="8" name="special_sensor_right" x="92" y="31" width="29.25" height="44.75">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="9" name="special_sensor_left" x="21.625" y="30.375" width="29.25" height="44.75">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="3" type="Character">
  <properties>
   <property name="aggroRadius" type="float" value="200"/>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1.5"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="attack2Damage" type="float" value="4"/>
   <property name="attack2DamageDelay" type="float" value="0.5"/>
   <property name="attack2Duration" type="float" value="1.5"/>
   <property name="attackDuration" type="float" value="1.2"/>
   <property name="attackRange" type="float" value="40"/>
   <property name="attackSound" value="SWING"/>
   <property name="damage" type="float" value="3"/>
   <property name="damageDelay" type="float" value="0.3"/>
   <property name="enemy" type="bool" value="true"/>
   <property name="graphicFlipOffsetX" type="float" value="-23"/>
   <property name="life" type="int" value="20"/>
   <property name="lifeReg" type="float" value="0.5"/>
   <property name="sortOffsetY" type="float" value="4"/>
   <property name="speed" type="float" value="2"/>
  </properties>
  <image source="../../Characters/enemy-wooden-rose.png" width="192" height="128"/>
  <objectgroup draworder="index" id="2">
   <object id="5" x="69.8938" y="87.2449" width="21.8125" height="2.875">
    <capsule/>
   </object>
   <object id="6" name="attack_sensor_right" x="94.1875" y="62.5625" width="15.875" height="20.25">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="7" name="attack2_sensor_right" x="117.727" y="52.5454" width="18.7273" height="36.9091">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="8" name="attack_sensor_left" x="85.1965" y="63.0431" width="14.0227" height="19.5455">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="9" name="attack2_sensor_left" x="49.6136" y="52.1705" width="18.25" height="35.25">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="4">
  <image source="../../../../../../assets_raw/objects/enemy-wooden-rose/attack_left_05.png" width="192" height="128"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="85.25" y="62.875" width="10.75" height="19"/>
  </objectgroup>
 </tile>
 <tile id="5">
  <image source="../../../../../../assets_raw/objects/enemy-wooden-rose/attack2_left_09.png" width="192" height="128"/>
  <objectgroup draworder="index" id="3">
   <object id="3" x="49.125" y="52.125" width="18.25" height="35.25"/>
  </objectgroup>
 </tile>
 <tile id="6" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="-23"/>
  </properties>
  <image source="../../Characters/blacksmith.png" width="96" height="96"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="58.75" y="60.5625">
    <polygon points="1.62364,1.46467 1.92527,-2.55027 1.06114,-6.34647 -1.17663,-9.10326 -4.9375,-9.6875 -15.625,-9.65139 -20.5,-8.6875 -22.3777,-6.43207 -22.3505,-3.03261 -21.8417,1.66428 -16.9857,3.43454 -1.81707,3.34832"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="7" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="-23"/>
  </properties>
  <image source="../../Characters/old-vendor.png" width="96" height="91"/>
 </tile>
 <tile id="8" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="15"/>
  </properties>
  <image source="../../../The Village/Characters/skillMaster.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="56.8182" y="114.818">
    <polygon points="0.123188,-4.96329 0.177778,4.21111 1.39293,4.1798 3.18182,4.18182 15.7036,-11.0553"/>
   </object>
   <object id="2" x="73.0625" y="96.25">
    <polygon points="0,0 -0.875,1.6875 -0.625,5.875 -1.21591,9.59659 -12.5778,17.6307 -12.8399,22.3958 2.84375,12.6875 3.74653,9.11597 4.65625,5.875 7,4.25 9.625,5.375 11.6875,8.3125 12.0625,11.3125 12.0625,12.375 12.1875,12.75 16.75,12.5625 16.625,11 15.6875,10.1875 15.8125,6.625 15.625,4.125 14.8438,1.75 13.8125,-0.59375 12.875,-2 9.75,-3.125 6.3125,-3.0625 2.4375,-2.6875 0.9375,-1.875 0.3125,-1"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="9" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="life" type="int" value="10"/>
   <property name="lifeReg" type="float" value="1"/>
   <property name="npc" type="bool" value="true"/>
   <property name="sortOffsetY" type="float" value="14"/>
   <property name="speed" type="float" value="1"/>
  </properties>
  <image source="../../../The Village/Characters/femVillager1.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="70.9091" y="103.909" width="16.0909" height="6">
    <ellipse/>
   </object>
   <object id="3" x="76.3636" y="84.6364" width="6.27273" height="18.5455">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="10" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="life" type="int" value="10"/>
   <property name="lifeReg" type="float" value="1"/>
   <property name="npc" type="bool" value="true"/>
   <property name="sortOffsetY" type="float" value="14"/>
   <property name="speed" type="float" value="1"/>
  </properties>
  <image source="../../../The Village/Characters/femVillager2.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="71.2045" y="103.75" width="16.0909" height="6">
    <ellipse/>
   </object>
   <object id="2" x="76.6136" y="83.3522" width="6.27273" height="18.5455">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="11" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="life" type="int" value="10"/>
   <property name="lifeReg" type="float" value="1"/>
   <property name="npc" type="bool" value="true"/>
   <property name="sortOffsetY" type="float" value="14"/>
   <property name="speed" type="float" value="1"/>
  </properties>
  <image source="../../../The Village/Characters/femVillager3.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="70.7045" y="103.625" width="16.0909" height="6">
    <ellipse/>
   </object>
   <object id="2" x="76.2386" y="83.1022" width="6.27273" height="18.5455">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="12" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="life" type="int" value="10"/>
   <property name="lifeReg" type="float" value="1"/>
   <property name="npc" type="bool" value="true"/>
   <property name="sortOffsetY" type="float" value="14"/>
   <property name="speed" type="float" value="1"/>
  </properties>
  <image source="../../../The Village/Characters/femVillager4.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="71.2045" y="103.5" width="16.0909" height="6">
    <ellipse/>
   </object>
   <object id="2" x="76.4886" y="83.8522" width="6.27273" height="18.5455">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="13" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="14"/>
  </properties>
  <image source="../../../The Village/Characters/blacksmith2.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="69.75" y="102.375">
    <polygon points="0.125,-0.198785 -0.210656,1.44525 0.239133,2.63788 0.253906,3.37891 0.264798,4.62983 -0.739104,4.63392 -0.789063,6.62262 3.91509,6.61821 5,2.625 6.12838,1.38598 8.5,0.125 12.1563,0.546875 13.2169,1.95701 14.0469,4.21875 14.3906,6.125 15.525,6.61667 20.2524,6.60495 20.2528,4.62529 19.2469,4.63069 19.2457,3.31936 19.228,1.25138 18.3432,0.378186 18.0272,-1.11413 20.0054,-3.07065 21.2473,-5.40217 20.3019,-7.99237 17.3696,-9.21739 11.3804,-10.3886 5.65217,-9.97826 1.65278,-7.975 0.34375,-4.90625 0.265625,-3.07813"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="14" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="14"/>
  </properties>
  <image source="../../../The Village/Characters/fishMan.png" width="192" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="89.75" y="98.5">
    <polygon points="0.688889,0.666667 0.152097,2.10211 -1.0625,3.375 -1.75,5.5 -1.46124,6.4878 -0.125,6.5 4.95069,6.49306 4.25,5.13281 9.0901,1.33647 10.0761,-0.570652 12.4402,-2.8587 13.6576,-5.43478 12.9348,-7.25543 10.4022,-8.20109 6.07609,-8.41304 2.28804,-8.41848 -0.36413,-7.85326 -2.03261,-6.83696 -3.25543,-5.1413 -3.84783,-3.47826 -3.6087,-1.49457 -2.02174,0.298913"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="16" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="14"/>
  </properties>
  <image source="../../../The Village/Characters/vendor2.png" width="128" height="128"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="56.0909" y="74.9091">
    <polygon points="0.0888889,0.0666667 -0.842348,3.09055 -1.15467,5.14886 -1.90554,6.75756 -2.30985,9.08219 1.17614,9.05682 2.78138,4.83137 6.36932,-0.15057 6.99666,1.2168 7.15605,5.28994 9.81027,8.083 9.81027,11.6561 11.0119,13.8498 21.2134,14.0435 22.9247,12.9417 22.8764,8.2287 25.8898,4.68699 25.8136,-0.502778 23.0662,-3.54989 19.7045,-5.44318 16.506,-5.90909 12.7549,-7.3892 9.08547,-8.09424 3.84152,-7.60783 1.48049,-6.19614 0.854545,-2.99394"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="17" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="14"/>
  </properties>
  <image source="../../../The Village/Characters/wizard.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="65.625" y="95.875">
    <polygon points="0.61413,3.92391 -0.160326,6.83696 0.875,9.5 6.46094,14.1016 10.345,14.1137 11.4017,12.1262 18.3864,12.1406 18.3811,13.1166 22.3734,13.1212 22.3747,12.1237 25.4256,12.1425 25.3906,14.103 27.3729,14.1244 27.375,10.375 27.375,6.96875 24.7935,4.02717 22.3071,2.4837 19.8288,1.63587 15.0353,1.125 9.72826,1.60326 4.77717,2.12228"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="18" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="life" type="int" value="13"/>
   <property name="lifeReg" type="float" value="1"/>
   <property name="npc" type="bool" value="true"/>
   <property name="sortOffsetY" type="float" value="14"/>
   <property name="speed" type="float" value="1"/>
  </properties>
  <image source="../../../The Village/Characters/maleVillager1.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="69.7159" y="103.852" width="18.125" height="6.25">
    <ellipse/>
   </object>
   <object id="2" x="73.3636" y="77.5795" width="11.25" height="24">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="19" type="Character">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="life" type="int" value="13"/>
   <property name="lifeReg" type="float" value="1"/>
   <property name="npc" type="bool" value="true"/>
   <property name="sortOffsetY" type="float" value="14"/>
   <property name="speed" type="float" value="1"/>
  </properties>
  <image source="../../../The Village/Characters/maleVillager4.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="69.6875" y="103.619" width="18.125" height="6.25">
    <ellipse/>
   </object>
   <object id="2" x="73.2841" y="77.7273" width="11.25" height="24">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="20" type="StaticProp">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="interactable" type="bool" value="true"/>
   <property name="interactionRadius" type="float" value="30"/>
   <property name="sortOffsetY" type="float" value="14"/>
  </properties>
  <image source="../../../The Village/Characters/vendor.png" width="160" height="160"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="70.875" y="102">
    <polygon points="0.046875,0.078125 -1.79861,1.8125 -1.81528,4.15972 -0.0666667,6.20972 2.1875,6.9375 6.5,6.6875 8.5625,7.6875 9.875,7.5625 10.6875,6.9375 14.125,7 16.0313,6.95313 17.75,5 19.5,5 24.375,7.4375 28.875,8.125 48.125,-2.09375 47.625,-6.125 46.625,-8.875 44.625,-10.375 40.0625,-10.9375 35.1875,-10.4375 27.875,-8.0625 21.6875,-5.9375 15.375,-3.9375 15.1875,-1.375 12.4932,-1.56386 8.125,-1.73777 3.63043,-1.10326"/>
   </object>
  </objectgroup>
 </tile>
</tileset>
