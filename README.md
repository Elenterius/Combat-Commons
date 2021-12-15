# Combat-Commons
Combat Commons is a library mod for Forge Minecraft that provides modifiable attack reach and other utilities related combat for other mod developers.

## Why
At the moment many mods provide their own solution to exteding the players attack reach, in most cases this causes incompatibilities due to conflicting mixins.

## Current Features
### Separate Attack Reach Attribute
  - independent of `block_reach` (as proposed in the abandoned forge pull request)
  - Pehkui 3 compatibility (2 should also work)
  - properly handles decreased attack reach 
  - allow items to determine which Block & Fluid Mode is used for the pick block and attack RayTrace
 
##### Creative Attack Reach Changes
`creative_attack_reach = (attack_reach + 3)`  (default attack reach = 3, creative mode now gives a flat increase)

##### Block Reach Changes
Fixed the forge "bug" where players with `zero block reach` are still able to hit the block that is present at their eye position (e.g. when inside tall grass/plants).

##### Server Side Distance Check Changes
Instead of  calculating the distance between the player and targeted Entity using their positions, we use the distance between the player position and the bounding box of the targeted Entity. This means players with reduced attack reach will still be able to attack Entities with large bounding boxes. (e.g. slime with a size of 10)
