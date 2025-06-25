package pojo;

import java.util.Arrays;

public class Order {
    private final String[] ingredients;

    public Order(String[] ingredients) {
        this.ingredients = ingredients;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    @Override
    public String toString() {
        return "{\"ingredients\":" + Arrays.toString(ingredients) + "}";
    }
}