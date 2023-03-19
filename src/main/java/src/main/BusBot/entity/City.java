package src.main.BusBot.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum City {
    Осиповичи(1),Минск(2),Бобруйск(3);

    private final int id;

    public int getId() {
        return id;
    }
}
