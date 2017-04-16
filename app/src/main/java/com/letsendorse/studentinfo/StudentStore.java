package com.letsendorse.studentinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;


public class StudentStore {
    private static StudentStore instance;
    private final Context context;
    public static final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();
    private final SharedPreferences sharedPreferences;
    private List<Student> list;
    private int idCounter = 0;

    // Using Android's base64 libraries. This can be replaced with any base64 library.
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decode(json.getAsString(), Base64.NO_WRAP);
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
        }
    }

    public StudentStore(Context context) {
        this.context = context;
        this.sharedPreferences = this.context.getSharedPreferences("data", 0);
    }

    public static StudentStore getInstance(Context context
    ) {
        if (instance == null) {
            instance = new StudentStore(context);
        }
        return instance;
    }

    public List<Student> getAll() {
        if (list == null) {
            list = loadStudents();
        }
        return list;

    }

    private List<Student> loadStudents() {
        String list = sharedPreferences.getString("list", "[]");
        Type type = new TypeToken<List<Student>>() {
        }.getType();
        List<Student> res = gson.fromJson(list, type);
        for (Student s : res) {
            if (s.getId() == 0) {
                s.setId(++idCounter);
            } else if (this.idCounter < s.getId()) {
                this.idCounter = s.getId();
            }
        }
        return res;
    }


    public void addStudent(Student student) {
        if (student.getId() == 0) {
            student.setId(++idCounter);
        }
        getAll().add(student);
        save();
    }

    private void save() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("list", gson.toJson(list));
        edit.commit();
    }

    public void updateStudent(Student student) {
        List<Student> list = getAll();
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(0).getId() == student.getId()) {
                index = i;
                break;
            }
        }
        list.set(index, student);
        save();
    }

    public void deleteStudent(Student student) {
        getAll().remove(student);
        save();
    }

}
