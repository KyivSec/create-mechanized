package kyivsec.createmechanized;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGenerators {
    private DataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new LanguageProvider(output, CreateMechanizedMod.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                add("itemGroup.createmechanized", "Create: Mechanized");
                add("createmechanized.configuration.title", "Create: Mechanized Configs");
                add("createmechanized.configuration.section.createmechanized.common.toml", "Create: Mechanized Configs");
                add("createmechanized.configuration.section.createmechanized.common.toml.title", "Create: Mechanized Configs");
                add("createmechanized.configuration.items", "Item List");
                add("createmechanized.configuration.logDirtBlock", "Log Dirt Block");
                add("createmechanized.configuration.magicNumberIntroduction", "Magic Number Text");
                add("createmechanized.configuration.magicNumber", "Magic Number");
            }
        });
    }
}
