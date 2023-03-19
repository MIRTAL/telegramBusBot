package src.main.BusBot.service.impl;

import src.main.BusBot.entity.City;
import src.main.BusBot.service.CityModeService;

import java.util.HashMap;
import java.util.Map;

public class HashMapCityModeService implements CityModeService {
    private final Map<Long, City> cityFrom = new HashMap<>();
    private final Map<Long, City> cityTo = new HashMap<>();

    public HashMapCityModeService() {
        System.out.println("HASHMAP MODE is created");
    }

    @Override
    public City getCityFrom(long chatId) {
        return cityFrom.getOrDefault(chatId, City.Осиповичи);
    }

    @Override
    public City getCityTo(long chatId) {
        return cityTo.getOrDefault(chatId, City.Минск);
    }

    @Override
    public void setCityFrom(long chatId, City city) {
        cityFrom.put(chatId, city);
    }

    @Override
    public void setCityTo(long chatId, City city) {
        cityTo.put(chatId, city);
    }
}