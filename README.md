# Combat-Commons
[![](https://jitpack.io/v/Elenterius/Combat-Commons.svg)](https://jitpack.io/#Elenterius/Combat-Commons)
[![](https://cf.way2muchnoise.eu/versions/For%20Minecraft_557441_all.svg)](https://www.curseforge.com/minecraft/mc-mods/combat-commons)

Combat Commons is a library mod for Forge Minecraft that provides modifiable attack reach and other utilities related combat for other mod developers.

### Maven

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.Elenterius:Combat-Commons:Tag'
}
```

## Why

At the moment many mods provide their own solution to exteding the players attack reach, in most cases this causes incompatibilities due to conflicting mixins.

## Current Features

- Attack Reach Attribute
- Enchantments that apply Attribute Modifiers
- Enchantments that have an `Entity` sensitive #getExtraDamage method

## Implementation Details
### Separate Attack Reach Attribute

- independent of `block_reach` (as proposed in the abandoned forge pull request #7808)
- Pehkui 3 compatibility (2 should also work)
- properly handles decreased attack reach
- allow items to determine which Block & Fluid Mode is used for the pick block and attack RayTrace
 
##### Creative Attack Reach Changes
`creative_attack_reach = (attack_reach + 3)`  (default attack reach = 3, creative mode now gives a flat increase)

##### Block Reach Changes
Fixed the forge "bug" where players with `zero block reach` are still able to hit the block that is present at their eye position (e.g. when inside tall grass/plants).

##### Server Side Distance Check Changes
Instead of  calculating the distance between the player and targeted Entity using their positions, we use the distance between the player position and the bounding box of the targeted Entity. This means players with reduced attack reach will still be able to attack Entities with large bounding boxes. (e.g. slime with a size of 10)

