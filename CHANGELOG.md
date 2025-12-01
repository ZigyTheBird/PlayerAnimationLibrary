## IMPORTANT!!
Remove the following line from your build.gradle if you're on NeoForge:  
`additionalRuntimeClasspath "org.javassist:javassist:3.30.2-GA"`

## Changes
Added a method for getting bones from the controller called `getBone`. (This allows you to enable/disable bone axes mid-animation)  
Blender animations now support particles keyframes.  
You can now disable an axis on Bedrock format animations by setting the first keyframe to "pal.disabled". (Look at disable_tests.json for an example.)

## Bug Fixes & Small Changes
Fixed an issue where you can't use Mocha (our Molang engine) classes without causing a crash.
Miscellaneous very minor performance improvements.  
Better logging when an animation is not found.  
Updated molang compiler.  
Fixed an issue with the mod's events and registries causing a crash on NeoForge.