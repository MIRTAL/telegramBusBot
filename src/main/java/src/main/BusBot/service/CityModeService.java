package src.main.BusBot.service;

import src.main.BusBot.entity.City;
import src.main.BusBot.service.impl.HashMapCityModeService;


public interface CityModeService {

    static CityModeService getInstance() {
        return new HashMapCityModeService();
    }

    City getCityFrom(long chatId);

    City getCityTo(long chatId);

    void setCityFrom(long chatId, City city);

    void setCityTo(long chatId, City city);
}
