package lf.vehicle;

public class Main {
    Wot wot = new DefaultWot();

    Thing thing = new Thing.Builder()
        .setId("counter")
        .setTitle("My Counter")
        .setDescription("This is a simple counter thing")
        .build();

    // Thing thing = Thing.fromJson(new File("path/to/thing.json"));

    wot.destroy();

}