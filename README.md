After you have joined a world at list once a there will be directory in your config called StgDel. Inside it there will be a file name StageNames.stg, in this file you want to write in your stage names. The way you do this is by opening the file with any text editor and write the stages in the following format (they do not have to be in the order of the progression).

```
Some stage name#0
Some other stage name#1
Something idk#2
```

There can be as many stages as you want. The numbers after the "#" don't have to be in order they only have to be a positive integer and distinct from each other, those represent the stage id.

After that you have to join any world again. Then in StgDel directory will be a file for each stage you have in StageDelimiter.stg, each having the name of the stage they represent and the .stg extension. Each one of those files is used for storeing the registry names of the result of the banned recipes for each stage.You have to put each separate registry name on a new line and if you are using craft tweaker for getting the registry names it doesn't matter if the names are between angle brackets.

```
minecraft:grass
<minecraft:ender_pearl>
minecraft:arrow
```

If you instead want to ban all the registry names from a certain mod or a set of registry names that share a common start you can use the character "!" at the start of a line to ban all registry names that start with that specific character sequence.

This bans everything that has a registry name that starts with "minecraft".

```
!minecraft
```

A more practical example would be this one:

```
!immersiveengineering
<minecraft:stained_glass:5>
<minecraft:stained_glass:6>
<minecraft:stained_glass:7>
<minecraft:stained_glass:8>
<minecraft:stained_glass:9>
<minecraft:stained_glass:10>
<minecraft:stained_glass:11>
<minecraft:stained_glass:12>
<minecraft:stained_glass:13>
<tfctech:pot_ash>
<tfctech:pot_potash>
<tfctech:powder/potash>
<tfctech:latex/vulcanizing_agents>
<tfctech:latex/rubber_mix>
<tfctech:latex/rubber>
<tfctech:wiredraw/leather_belt>
<tfctech:metal/copper_inductor>
<tfctech:metal/wrought_iron_blowpipe>
<tfctech:metal/steel_blowpipe>
<tfctech:metal/black_steel_blowpipe>
<tfctech:metal/blue_steel_blowpipe>
<tfctech:metal/red_steel_blowpipe>
<tfctech:metal/aluminium_blowpipe>
<tfctech:electric_forge>
<tfctech:induction_crucible>
<tfctech:smeltery_cauldron>
<tfctech:smeltery_firebox>
<tfctech:fridge>
<minecraft:glass>
<minecraft:stained_glass>
<minecraft:stained_glass:1>
<minecraft:stained_glass:2>
<minecraft:stained_glass:3>
<minecraft:stained_glass:4>
<minecraft:stained_glass:5>
<minecraft:stained_glass:6>
<minecraft:stained_glass:7>
<minecraft:stained_glass:8>
<minecraft:stained_glass:9>
<minecraft:stained_glass:10>
<minecraft:stained_glass:11>
<minecraft:stained_glass:12>
<minecraft:stained_glass:13>
<tfctech:pot_ash>
<tfctech:pot_potash>
<tfctech:powder/potash>
<tfctech:latex/vulcanizing_agents>
<tfctech:latex/rubber_mix>
<tfctech:latex/rubber>
<tfctech:wiredraw/leather_belt>
<tfctech:metal/copper_inductor>
<tfctech:metal/wrought_iron_blowpipe>
<tfctech:metal/steel_blowpipe>
<tfctech:metal/black_steel_blowpipe>
<tfctech:metal/blue_steel_blowpipe>
<tfctech:metal/red_steel_blowpipe>
<tfctech:metal/aluminium_blowpipe>
<tfctech:electric_forge>
<tfctech:induction_crucible>
<tfctech:smeltery_cauldron>
<tfctech:smeltery_firebox>
<tfctech:fridge>
<minecraft:glass>
<minecraft:stained_glass>
<minecraft:stained_glass:1>
<minecraft:stained_glass:2>
<minecraft:stained_glass:3>
<minecraft:stained_glass:4>
<minecraft:stained_glass:14>
<minecraft:stained_glass:15>
<minecraft:glass_pane>
<minecraft:stained_glass_pane>
<minecraft:stained_glass_pane:1>
<minecraft:stained_glass_pane:2>
<minecraft:stained_glass_pane:3>
<minecraft:stained_glass_pane:4>
<minecraft:stained_glass_pane:5>
<minecraft:stained_glass_pane:6>
<minecraft:stained_glass_pane:7>
<minecraft:stained_glass_pane:8>
<minecraft:stained_glass_pane:9>
<minecraft:stained_glass_pane:10>
<minecraft:stained_glass_pane:11>
<minecraft:stained_glass_pane:12>
<minecraft:stained_glass_pane:13>
<minecraft:stained_glass_pane:14>
<minecraft:stained_glass_pane:15>
<minecraft:glass_bottle>
```

Kinda long but i think it makes it clear how things work. The first line blocks everything from immersive enginnering since it blocks all registry names that start with "immersiveengineering" and the next lines simply ban various recipes.

