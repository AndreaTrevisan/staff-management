package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.AbsenceService;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = Routes.CALENDAR, layout = MainLayout.class)
public class CalendarView extends BasicLoggedInView {

    private final ContractService contractService;
    private final AbsenceService absenceService;
    private final PersonService personService;

    private final DatePicker monthPicker = new DatePicker();
    private final Grid<WeekRow> calendarGrid = new Grid<>(WeekRow.class, false);

    private final Map<LocalDate, List<Span>> eventsByDate = new HashMap<>();
    private final Map<String, String> personInfoCache = new HashMap<>();

    public CalendarView(ContractService contractService, AbsenceService absenceService, PersonService personService) {
        this.contractService = contractService;
        this.absenceService = absenceService;
        this.personService = personService;

        setSizeFull();

        monthPicker.setLabel("Select Month");
        monthPicker.addValueChangeListener(e -> refreshCalendar());
        add(monthPicker);

        buildGrid();

        monthPicker.setValue(LocalDate.now());
        refreshCalendar();
    }

    private void buildGrid() {
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            calendarGrid.addColumn(new ComponentRenderer<>(row -> {
                LocalDate date = row.getDay(dayIndex);
                if (date == null) {
                    return new VerticalLayout();
                }

                VerticalLayout cell = new VerticalLayout();
                cell.setPadding(false);
                cell.setSpacing(false);

                Span dayLabel = new Span(String.valueOf(date.getDayOfMonth()));
                dayLabel.getStyle().set("font-weight", "bold");
                cell.add(dayLabel);

                getEventsForDate(date).forEach(cell::add);
                return cell;
            })).setHeader(dayName(i));
        }

        calendarGrid.setSizeFull();
        add(calendarGrid);
        expand(calendarGrid);
    }

    private String dayName(int index) {
        switch (index) {
            case 0:
                return "Mon";
            case 1:
                return "Tue";
            case 2:
                return "Wed";
            case 3:
                return "Thu";
            case 4:
                return "Fri";
            case 5:
                return "Sat";
            case 6:
                return "Sun";
            default:
                return "";
        }
    }

    private void refreshCalendar() {
        LocalDate selected = monthPicker.getValue();
        if (selected == null) {
            selected = LocalDate.now();
        }

        LocalDate firstDay = selected.withDayOfMonth(1);
        LocalDate lastDay = selected.withDayOfMonth(selected.lengthOfMonth());

        rebuildEventsIndex(firstDay, lastDay);

        List<WeekRow> weeks = new ArrayList<>();
        WeekRow currentWeek = new WeekRow();

        int firstWeekDay = firstDay.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < firstWeekDay; i++) {
            currentWeek.setDay(i, null);
        }

        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {
            int col = d.getDayOfWeek().getValue() - 1;
            currentWeek.setDay(col, d);
            if (col == 6) {
                weeks.add(currentWeek);
                currentWeek = new WeekRow();
            }
        }

        boolean weekHasDays = false;
        for (int i = 0; i < 7; i++) {
            if (currentWeek.getDay(i) != null) {
                weekHasDays = true;
                break;
            }
        }
        if (weekHasDays) {
            weeks.add(currentWeek);
        }

        calendarGrid.setItems(weeks);
    }

    private void rebuildEventsIndex(LocalDate start, LocalDate end) {
        eventsByDate.clear();
        personInfoCache.clear();

        for (AbsenceDTO absence : absenceService.getAllAbsences()) {
            LocalDate date = absence.getDate();
            if (date == null || date.isBefore(start) || date.isAfter(end)) {
                continue;
            }

            String personInfo = getPersonInfo(absence.getPersonDocumentId());
            Span event = styledEvent(personInfo + " - " + absence.getReason(), "#ff4d4d");
            event.getElement().setAttribute("title", "Person: " + personInfo + "\n"
                    + "Reason: " + absence.getReason() + "\n"
                    + "Date: " + date);
            eventsByDate.computeIfAbsent(date, key -> new ArrayList<>()).add(event);
        }

        for (ContractDTO contract : contractService.getAllContracts()) {
            String personInfo = getPersonInfo(contract.getPersonDocumentId());

            addContractEvent(contract.getStartDate(), start, end,
                    styledEvent(personInfo + " START", "#4da6ff"),
                    "Person: " + personInfo + "\n"
                            + "Start Date: " + contract.getStartDate() + "\n"
                            + "Job Role: " + contract.getJobRole());

            addContractEvent(contract.getEndDate(), start, end,
                    styledEvent(personInfo + " END", "#004080"),
                    "Person: " + personInfo + "\n"
                            + "End Date: " + contract.getEndDate() + "\n"
                            + "Job Role: " + contract.getJobRole());

            addContractEvent(contract.getLicenseExpiry(), start, end,
                    styledEvent(personInfo + " LICENSE", "#ffaa00"),
                    "Person: " + personInfo + "\n"
                            + "License Expiry: " + contract.getLicenseExpiry() + "\n"
                            + "Job Role: " + contract.getJobRole());
        }
    }

    private void addContractEvent(LocalDate date, LocalDate start, LocalDate end, Span span, String tooltipText) {
        if (date == null || date.isBefore(start) || date.isAfter(end)) {
            return;
        }
        span.getElement().setAttribute("title", tooltipText);
        eventsByDate.computeIfAbsent(date, key -> new ArrayList<>()).add(span);
    }

    private Span styledEvent(String text, String backgroundColor) {
        Span span = new Span(text);
        span.getStyle().set("background-color", backgroundColor);
        span.getStyle().set("color", "white");
        span.getStyle().set("padding", "2px 4px");
        span.getStyle().set("border-radius", "4px");
        span.getStyle().set("display", "inline-block");
        span.getStyle().set("margin", "1px");
        return span;
    }

    private List<Span> getEventsForDate(LocalDate date) {
        return eventsByDate.containsKey(date) ? eventsByDate.get(date) : Collections.<Span>emptyList();
    }

    private String getPersonInfo(String documentId) {
        if (documentId == null) {
            return "N/A";
        }

        return personInfoCache.computeIfAbsent(documentId, key -> personService.getPerson(key)
                .map(p -> p.getName() + " " + p.getSurname() + " (" + p.getDocumentId() + ")")
                .orElse(key));
    }

    public static class WeekRow {
        private final LocalDate[] days = new LocalDate[7];

        public LocalDate getDay(int i) {
            return days[i];
        }

        public void setDay(int i, LocalDate date) {
            days[i] = date;
        }
    }

    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }
}
