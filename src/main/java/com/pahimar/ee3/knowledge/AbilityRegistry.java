package com.pahimar.ee3.knowledge;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pahimar.ee3.exchange.WrappedStack;
import com.pahimar.ee3.reference.Files;
import com.pahimar.ee3.util.SerializationHelper;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class AbilityRegistry implements JsonSerializer<AbilityRegistry>, JsonDeserializer<AbilityRegistry>
{
    private static final Gson jsonSerializer = (new GsonBuilder()).setPrettyPrinting().registerTypeAdapter(AbilityRegistry.class, new AbilityRegistry()).create();
    private static AbilityRegistry abilityRegistry = null;

    private static File abilityDirectory;
    private boolean hasBeenModified;
    private Set<WrappedStack> notLearnableSet;
    private Set<WrappedStack> notRecoverableSet;

    private AbilityRegistry()
    {
        abilityDirectory = new File(SerializationHelper.getDataDirectory(), "abilities");
        abilityDirectory.mkdirs();

        hasBeenModified = false;
        notLearnableSet = new TreeSet<WrappedStack>();
        notRecoverableSet = new TreeSet<WrappedStack>();
    }

    public static AbilityRegistry getInstance()
    {
        if (abilityRegistry == null)
        {
            abilityRegistry = new AbilityRegistry();
            abilityRegistry.init();
        }

        return abilityRegistry;
    }

    private void init()
    {
        notLearnableSet = new TreeSet<WrappedStack>();
        notRecoverableSet = new TreeSet<WrappedStack>();
    }

    public Set<WrappedStack> getNotLearnableStacks()
    {
        return this.notLearnableSet;
    }

    public boolean isLearnable(Object object)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            WrappedStack wrappedObject = new WrappedStack(object);
            return !notLearnableSet.contains(wrappedObject);
        }

        return false;
    }

    public void setAsLearnable(Object object)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            hasBeenModified = notLearnableSet.remove(new WrappedStack(object));
        }
    }

    public void setAsNotLearnable(Object object)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            hasBeenModified = notLearnableSet.add(new WrappedStack(object));
        }
    }

    public Set<WrappedStack> getNotRecoverableSet()
    {
        return this.notRecoverableSet;
    }

    public boolean isRecoverable(Object object)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            return !notRecoverableSet.contains(new WrappedStack(object));
        }

        return false;
    }

    public void setAsRecoverable(Object object)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            hasBeenModified = notRecoverableSet.remove(new WrappedStack(object));
        }
    }

    public void setAsNotRecoverable(Object object)
    {
        if (WrappedStack.canBeWrapped(object))
        {
            hasBeenModified = notRecoverableSet.add(new WrappedStack(object));
        }
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Not Learnables: ");
        for (WrappedStack wrappedStack : notLearnableSet)
        {
            stringBuilder.append(wrappedStack + " ");
        }
        stringBuilder.append(", Not Recoverables: ");
        for (WrappedStack wrappedStack : notRecoverableSet)
        {
            stringBuilder.append(wrappedStack + " ");
        }

        return stringBuilder.toString();
    }

    @Override
    public AbilityRegistry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonObject())
        {
            JsonObject jsonObject = (JsonObject) json;

            Set<WrappedStack> notLearnableStacks = new TreeSet<WrappedStack>();
            Set<WrappedStack> notRecoverableStacks = new TreeSet<WrappedStack>();

            if (jsonObject.has("notLearnable") && jsonObject.get("notLearnable").isJsonArray())
            {
                JsonArray jsonArray = (JsonArray) jsonObject.get("notLearnable");
                Iterator<JsonElement> iterator = jsonArray.iterator();

                while (iterator.hasNext())
                {
                    JsonElement jsonElement = iterator.next();
                    WrappedStack wrappedStack = WrappedStack.jsonSerializer.fromJson(jsonElement, WrappedStack.class);

                    if (wrappedStack != null)
                    {
                        notLearnableStacks.add(wrappedStack);
                    }
                }
            }

            if (jsonObject.has("notRecoverable") && jsonObject.get("notRecoverable").isJsonArray())
            {
                JsonArray jsonArray = (JsonArray) jsonObject.get("notRecoverable");
                Iterator<JsonElement> iterator = jsonArray.iterator();

                while (iterator.hasNext())
                {
                    JsonElement jsonElement = iterator.next();
                    WrappedStack wrappedStack = WrappedStack.jsonSerializer.fromJson(jsonElement, WrappedStack.class);

                    if (wrappedStack != null)
                    {
                        notRecoverableStacks.add(wrappedStack);
                    }
                }
            }

            AbilityRegistry abilityRegistry1 = new AbilityRegistry();
            abilityRegistry1.notLearnableSet = notLearnableStacks;
            abilityRegistry1.notRecoverableSet = notRecoverableStacks;
            return abilityRegistry1;
        }

        return null;
    }

    @Override
    public JsonElement serialize(AbilityRegistry abilityRegistry, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonAbilityRegistry = new JsonObject();

        JsonArray notLearnables = new JsonArray();
        for (WrappedStack wrappedStack : abilityRegistry.getNotLearnableStacks())
        {
            notLearnables.add(WrappedStack.jsonSerializer.toJsonTree(wrappedStack));
        }
        jsonAbilityRegistry.add("notLearnable", notLearnables);

        JsonArray notRecoverables = new JsonArray();
        for (WrappedStack wrappedStack : abilityRegistry.getNotRecoverableSet())
        {
            notRecoverables.add(WrappedStack.jsonSerializer.toJsonTree(wrappedStack));
        }
        jsonAbilityRegistry.add("notRecoverable", notRecoverables);

        return jsonAbilityRegistry;
    }

    public void saveAbilityRegistryToFile()
    {
        if (abilityDirectory != null)
        {
            abilityDirectory.mkdirs();
            writeToFile(new File(abilityDirectory, Files.ABILITIES_JSON_FILE));
        }
    }

    private void writeToFile(File file)
    {
        JsonWriter jsonWriter;

        if (hasBeenModified)
        {
            try
            {
                jsonWriter = new JsonWriter(new FileWriter(file));
                jsonWriter.setIndent("    ");
                jsonSerializer.toJson(this, AbilityRegistry.class, jsonWriter);
                jsonWriter.close();
                hasBeenModified = false;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void loadAbilityRegistryFromFile(boolean loadFileOnly)
    {
        if (abilityDirectory != null)
        {
            File abilitiesFile = new File(abilityDirectory, Files.ABILITIES_JSON_FILE);

            if (abilitiesFile.exists())
            {
                readFromFile(abilitiesFile, loadFileOnly);
            }
        }
    }

    private void readFromFile(File file, boolean loadFileOnly)
    {
        JsonReader jsonReader;

        try
        {
            jsonReader = new JsonReader(new FileReader(file));
            AbilityRegistry abilityRegistry1 = jsonSerializer.fromJson(jsonReader, AbilityRegistry.class);
            jsonReader.close();

            if (!loadFileOnly)
            {
                for (WrappedStack wrappedStack : abilityRegistry1.getNotLearnableStacks())
                {
                    if (!this.notLearnableSet.contains(wrappedStack))
                    {
                        this.notLearnableSet.add(wrappedStack);
                    }
                }

                for (WrappedStack wrappedStack : abilityRegistry1.getNotRecoverableSet())
                {
                    if (!this.notRecoverableSet.contains(wrappedStack))
                    {
                        this.notRecoverableSet.add(wrappedStack);
                    }
                }
            }
            else
            {
                this.notLearnableSet = abilityRegistry1.notLearnableSet;
                this.notRecoverableSet = abilityRegistry1.notRecoverableSet;
            }

            hasBeenModified = true;
        }
        catch (FileNotFoundException ignored)
        {
            // NOOP
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
