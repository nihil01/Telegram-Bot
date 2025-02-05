package world.horosho.tgbot.database.models;

import java.time.LocalDate;

public class CalendarData {
    private LocalDate weekendWork;
    private LocalDate holidays;

    public CalendarData(LocalDate weekendWork, LocalDate holidays) {
        this.weekendWork = weekendWork;
        this.holidays = holidays;
    }

    public LocalDate getWeekendWork() {
        return weekendWork;
    }

    public void setWeekendWork(LocalDate weekendWork) {
        this.weekendWork = weekendWork;
    }

    public LocalDate getHolidays() {
        return holidays;
    }

    public void setHolidays(LocalDate holidays) {
        this.holidays = holidays;
    }

    @Override
    public String toString() {
        return "CalendarData{" +
                "weekendWork=" + weekendWork +
                ", holidays=" + holidays +
                '}';
    }
}
