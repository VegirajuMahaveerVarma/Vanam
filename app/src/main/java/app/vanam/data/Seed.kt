package app.vanam.data
object Seed {
    private fun s(id: String, c: String, te: String?, sci: String, type: String, marks: String, hab: String, season: String, cue: String) =
        SpeciesEntity(id, c, te, sci, type, marks, hab, season, cue, null)
    val species = listOf(
        s("red_vented_bulbul","Red-vented Bulbul",null,"Pycnonotus cafer","bird","Black head with a small crest. Brown scaly body. White rump and a crimson patch under the tail.","Gardens, scrub, roadsides","Year-round","Look for the red patch beneath the tail as it flits away."),
        s("indian_robin","Indian Robin",null,"Copsychus fulicatus","bird","Male is glossy black with a white shoulder patch and a chestnut patch under the tail. Tail often cocked upright.","Rocky scrub, dry fields","Year-round","Watch for the upright tail and white shoulder stripe."),
        s("purple_sunbird","Purple Sunbird",null,"Cinnyris asiaticus","bird","Tiny with a curved bill. Breeding male shimmers metallic purple.","Flowering gardens, scrub","Year-round; males brightest in summer","Look for a tiny bird probing flowers with its curved bill."),
        s("indian_peafowl","Indian Peafowl","నెమలి","Pavo cristatus","bird","Large. Male has a blue neck and long train. Female is brown with a pale throat.","Open woodland, farmland edges","Year-round; display in monsoon","Listen for the loud, far-carrying call."),
        s("common_myna","Common Myna","గోరింక","Acridotheres tristis","bird","Brown body, black hood, bright yellow bill and eye-patch. White wing flash in flight.","Towns, fields, roadsides","Year-round","Look for the yellow eye-patch and the white flash on the wing."),
        s("rose_ringed_parakeet","Rose-ringed Parakeet","రామచిలుక","Psittacula krameri","bird","Bright green with a long tail and red bill. Male has a black and rose collar.","Town trees, farmland","Year-round","Listen for shrill screeches, then look for green flashing past."),
        s("asian_koel","Asian Koel","కోకిల","Eudynamys scolopaceus","bird","Male is glossy black with a pale bill and red eye. Female is spotted brown and white.","Leafy trees in towns and orchards","Calls most in spring and summer","Listen for the rising ku-oo call, repeated and louder."),
        s("oriental_magpie_robin","Oriental Magpie-Robin",null,"Copsychus saularis","bird","Black and white with a long tail often held up. Male has a glossy black head and chest.","Gardens, light woodland","Year-round","Look for a black and white bird flicking its tail up on a low perch."),
        s("neem","Neem","వేప","Azadirachta indica","plant","Compound leaves with toothed leaflets. Small white fragrant flowers.","Roadsides, village edges","Flowers in spring","Crush a leaf: the smell is sharply bitter."),
        s("tamarind","Tamarind","చింత","Tamarindus indica","plant","Feathery paired leaflets. Brown curved pods.","Avenues, farms","Pods ripen in late winter","Count the many small paired leaflets along each leaf stalk."),
        s("gulmohar","Gulmohar","గుల్మోహర్","Delonix regia","plant","Fern-like leaves. Wide umbrella crown. Scarlet-orange flowers.","Streets, parks","Blooms in summer","Look for long dark seed pods on the branches."),
        s("flame_of_the_forest","Flame-of-the-forest","మోదుగ","Butea monosperma","plant","Leaves in three parts. Bright orange-red curved flowers on bare branches. Velvety flat pods.","Dry scrub forest, open land","Flowers in late winter and spring","Look for orange-red flowers on leafless branches."),
        s("banyan","Banyan","మర్రి","Ficus benghalensis","plant","Huge spreading tree with roots hanging from the branches. Oval leathery leaves. Small red figs.","Roadsides, temple grounds","Figs through much of the year","Look for thick roots dropping from the branches to the ground."),
        s("peepal","Peepal","రావి","Ficus religiosa","plant","Heart-shaped leaves with a long drawn-out tip. Smooth pale bark.","Temples, roadsides","New copper-pink leaves in early spring","Watch for leaves trembling even in a light breeze."),
        s("pongamia","Pongamia","కానుగ","Pongamia pinnata","plant","Glossy compound leaves. Clusters of small pink-white flowers. Flat, hard pods.","Avenues, roadsides","Flowers in spring","Look for flat smooth pods and glossy leaves in cool shade."))
    private fun c(id: Int, t: String, kind: String, tod: String = "any", season: String = "any") = ChallengeEntity(id, t, kind, tod, season)
    val challenges = listOf(
        c(1,"Find a leaf with three shades of green.","find"), c(2,"Stand still for one minute. Name two sounds.","hear"),
        c(3,"Touch rough bark, then smooth bark.","touch"), c(4,"Spot something that moves only when you stop moving.","see","morning"),
        c(5,"Find a seed that travels by wind or by wing.","find"), c(6,"Find a spider web. See how it is anchored.","see"),
        c(7,"Count the different bird calls before you walk on.","hear","morning"), c(8,"Find a fuzzy leaf, then a waxy one.","touch"),
        c(9,"Find three leaf shapes within ten steps.","find"), c(10,"Watch the sky for birds heading home.","see","evening"),
        c(11,"Follow a moving shadow for a minute.","see","day"), c(12,"Find something a bird or insect has already eaten.","find"),
        c(13,"Close your eyes for 30 seconds. Point to the farthest sound.","hear"), c(14,"Find a sun-warmed stone and one in shade.","touch"),
        c(15,"Find a plant growing through a crack.","find","any","monsoon"), c(16,"Find a flower with an insect visiting.","see","day","summer"),
        c(17,"Find a leaf that has started to change colour.","find","any","winter"), c(18,"Find the smallest living thing you can spot.","see"),
        c(19,"Find where the first evening insect is singing.","hear","evening"), c(20,"Find a feather, or a place a bird might perch.","find"))
}
