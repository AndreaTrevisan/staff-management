package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.AbsenceService;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = Routes.CALENDAR, layout = MainLayout.class)
public class CalendarView extends BasicLoggedInView {

    private final ContractService contractService;
    private final AbsenceService absenceService;
    private final PersonService personService;

    private final DatePicker monthPicker = new DatePicker();
    private final Grid<WeekRow> calendarGrid = new Grid<>(WeekRow.class, false);

    public CalendarView(ContractService contractService, AbsenceService absenceService, PersonService personService) {
        this.contractService = contractService;
        this.absenceService = absenceService;
        this.personService = personService;

        setSizeFull();

        // Selettore mese
        monthPicker.setLabel("Select Month");
        monthPicker.addValueChangeListener(e -> refreshCalendar());
        add(monthPicker);

        buildGrid();

        // inizializza con oggi
        monthPicker.setValue(LocalDate.now());
        refreshCalendar();
    }

    private void buildGrid() {
        // 7 colonne = giorni della settimana
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            calendarGrid.addColumn(new ComponentRenderer<>(row -> {
                LocalDate date = row.getDay(dayIndex);
                if (date == null) return new VerticalLayout(); // celle vuote

                VerticalLayout cell = new VerticalLayout();
                cell.setPadding(false);
                cell.setSpacing(false);

                // numero giorno
                Span dayLabel = new Span(String.valueOf(date.getDayOfMonth()));
                dayLabel.getStyle().set("font-weight", "bold");
                cell.add(dayLabel);

                // eventi
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
            case 0 : return "Mon";
            case 1 : return "Tue";
            case 2 : return "Wed";
            case 3 : return "Thu";
            case 4 : return "Fri";
            case 5 : return "Sat";
            case 6 : return "Sun";
            default : return "";
        }
    }

    private void refreshCalendar() {
        LocalDate selected = monthPicker.getValue();
        if (selected == null) selected = LocalDate.now();

        LocalDate firstDay = selected.withDayOfMonth(1);
        LocalDate lastDay = selected.withDayOfMonth(selected.lengthOfMonth());

        List<WeekRow> weeks = new ArrayList<>();
        WeekRow currentWeek = new WeekRow();

        // allineamento al primo giorno della settimana (Monday = 0)
        int firstWeekDay = firstDay.getDayOfWeek().getValue() % 7; // LUN=1->0, DOM=7->6
        for (int i = 0; i < firstWeekDay; i++) currentWeek.setDay(i, null);

        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {
            int col = (d.getDayOfWeek().getValue() % 7);
            currentWeek.setDay(col, d);
            if (col == 6) { // fine settimana
                weeks.add(currentWeek);
                currentWeek = new WeekRow();
            }
        }

        if (!weeks.contains(currentWeek)) weeks.add(currentWeek);

        calendarGrid.setItems(weeks);
    }

    private List<Span> getEventsForDate(LocalDate date) {
        List<Span> spans = new ArrayList<>();

        // Assenze
        List<AbsenceDTO> absences = absenceService.getAllAbsences();
        for (AbsenceDTO a : absences) {
            if (date.equals(a.getDate())) {
                String personInfo = getPersonInfo(a.getPersonDocumentId());

                // testo evento
                Span s = new Span(personInfo + " - " + a.getReason());
                s.getStyle().set("background-color", "#ff4d4d"); // rosso
                s.getStyle().set("color", "white");
                s.getStyle().set("padding", "2px 4px");
                s.getStyle().set("border-radius", "4px");
                s.getStyle().set("display", "inline-block");
                s.getStyle().set("margin", "1px");

                // tooltip testuale semplice (title)
                String tooltipText = "Person: " + personInfo + "\n"
                        + "Reason: " + a.getReason() + "\n"
                        + "Date: " + a.getDate();
                s.getElement().setAttribute("title", tooltipText);

                spans.add(s);
            }
        }

        // Contratti
        List<ContractDTO> contracts = contractService.getAllContracts();
        for (ContractDTO c : contracts) {
            String personInfo = getPersonInfo(c.getPersonDocumentId());

            if (date.equals(c.getStartDate())) {
                Span s = new Span(personInfo + " START");
                s.getStyle().set("background-color", "#4da6ff"); // blu
                s.getStyle().set("color", "white");
                s.getStyle().set("padding", "2px 4px");
                s.getStyle().set("border-radius", "4px");
                s.getStyle().set("display", "inline-block");
                s.getStyle().set("margin", "1px");

                String tooltipText = "Person: " + personInfo + "\n"
                        + "Start Date: " + c.getStartDate() + "\n"
                        + "Job Role: " + c.getJobRole();
                s.getElement().setAttribute("title", tooltipText);

                spans.add(s);
            }
            if (c.getEndDate() != null && date.equals(c.getEndDate())) {
                Span s = new Span(personInfo + " END");
                s.getStyle().set("background-color", "#004080"); // blu scuro
                s.getStyle().set("color", "white");
                s.getStyle().set("padding", "2px 4px");
                s.getStyle().set("border-radius", "4px");
                s.getStyle().set("display", "inline-block");
                s.getStyle().set("margin", "1px");

                String tooltipText = "Person: " + personInfo + "\n"
                        + "End Date: " + c.getEndDate() + "\n"
                        + "Job Role: " + c.getJobRole();
                s.getElement().setAttribute("title", tooltipText);

                spans.add(s);
            }
            if (c.getLicenseExpiry() != null && date.equals(c.getLicenseExpiry())) {
                Span s = new Span(personInfo + " LICENSE");
                s.getStyle().set("background-color", "#ffaa00"); // arancione
                s.getStyle().set("color", "white");
                s.getStyle().set("padding", "2px 4px");
                s.getStyle().set("border-radius", "4px");
                s.getStyle().set("display", "inline-block");
                s.getStyle().set("margin", "1px");

                String tooltipText = "Person: " + personInfo + "\n"
                        + "License Expiry: " + c.getLicenseExpiry() + "\n"
                        + "Job Role: " + c.getJobRole();
                s.getElement().setAttribute("title", tooltipText);

                spans.add(s);
            }
        }

        return spans;
    }
    private String getPersonInfo(String documentId) {
        if (documentId == null) return "N/A";
        Optional<PersonDTO> personOpt = personService.getPerson(documentId);
        return personOpt.map(p -> p.getName() + " " + p.getSurname() + " (" + p.getDocumentId() + ")")
                .orElse(documentId);
    }

    // Helper class per una riga di 7 giorni
    public static class WeekRow {
        private final LocalDate[] days = new LocalDate[7];
        public LocalDate getDay(int i) { return days[i]; }
        public void setDay(int i, LocalDate date) { days[i] = date; }
    }

    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }
}