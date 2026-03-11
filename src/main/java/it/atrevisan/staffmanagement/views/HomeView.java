package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.AbsenceService;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import it.atrevisan.staffmanagement.views.session.SessionUtils;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = Routes.HOME, layout = MainLayout.class)
public class HomeView extends BasicLoggedInView {

    private final PersonService personService;
    private final ContractService contractService;
    private final AbsenceService absenceService;

    private final DatePicker startDatePicker = new DatePicker("From");
    private final DatePicker endDatePicker = new DatePicker("To");

    public HomeView(PersonService personService,
                        ContractService contractService,
                        AbsenceService absenceService) {
        this.personService = personService;
        this.contractService = contractService;
        this.absenceService = absenceService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        buildDashboard();
    }

    private void buildDashboard() {
        removeAll();

        Optional<UserDTO> currentUserOpt = SessionUtils.getSessionUser() == null
                ? Optional.empty()
                : Optional.of(SessionUtils.getSessionUser());

        if (!currentUserOpt.isPresent()) {
            add(new Span("No user logged in"));
            return;
        }

        UserDTO currentUser = currentUserOpt.get();

        H2 welcome = new H2("Welcome, " + currentUser.getUsername());
        add(welcome);

        // Layout principale a riquadri
        FlexLayout dashboardLayout = new FlexLayout();
        dashboardLayout.setSizeFull();
        dashboardLayout.setFlexDirection(FlexDirection.ROW);
        dashboardLayout.setFlexWrap(FlexWrap.WRAP);
        dashboardLayout.setJustifyContentMode(JustifyContentMode.START);
        dashboardLayout.setAlignItems(Alignment.START);

        // Card Ruoli
        VerticalLayout rolesCard = buildRolesCard(currentUser);
        // Card Info Persona
        VerticalLayout personCard = buildPersonCard(currentUser);
        // Card Contratti
        VerticalLayout contractCard = buildContractsCard(currentUser);
        // Card Assenze
        VerticalLayout absenceCard = buildAbsencesCard(currentUser);

        dashboardLayout.add(rolesCard, personCard, contractCard, absenceCard);
        add(dashboardLayout);
    }

    private VerticalLayout buildRolesCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("border-radius", "6px");
        card.getStyle().set("background-color", "#f5f5f5");

        card.add(new H2("User Roles"));

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> card.add(new Span(role)));
        } else {
            card.add(new Span("No roles assigned"));
        }

        return card;
    }

    private VerticalLayout buildPersonCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("border-radius", "6px");
        card.getStyle().set("background-color", "#f5f5f5");

        card.add(new H2("Person Info"));

        if (user.getPersonDocumentId() != null) {
            Optional<PersonDTO> personOpt = personService.getPerson(user.getPersonDocumentId());
            if (personOpt.isPresent()) {
                PersonDTO p = personOpt.get();
                card.add(new Span("Name: " + p.getName() + " " + p.getSurname()));
                card.add(new Span("Email: " + p.getEmail()));
                card.add(new Span("Phone: " + p.getPhone()));
                card.add(new Span("Address: " + p.getAddress()));
            } else {
                card.add(new Span("Person info not found"));
            }
        } else {
            card.add(new Span("No person associated"));
        }

        return card;
    }

    private VerticalLayout buildContractsCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("border-radius", "6px");
        card.getStyle().set("background-color", "#f5f5f5");

        card.add(new H2("Active Contracts"));

        if (user.getPersonDocumentId() != null) {
            List<ContractDTO> contracts = contractService.getAllContracts()
                    .stream()
                    .filter(c -> c.getPersonDocumentId().equals(user.getPersonDocumentId())
                            && (c.getEndDate() == null || c.getEndDate().isAfter(LocalDate.now())))
                    .collect(Collectors.toList());

            if (contracts.isEmpty()) {
                card.add(new Span("No active contracts"));
            } else {
                contracts.forEach(c -> {
                    card.add(new Span("Role: " + c.getJobRole()));
                    card.add(new Span("Start: " + c.getStartDate() + " - End: " + (c.getEndDate() != null ? c.getEndDate() : "Ongoing")));
                });
            }
        } else {
            card.add(new Span("No person associated"));
        }

        return card;
    }

    private VerticalLayout buildAbsencesCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("400px");
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("border-radius", "6px");
        card.getStyle().set("background-color", "#f5f5f5");

        card.add(new H2("Absences"));

        HorizontalLayout filterLayout = new HorizontalLayout();
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        Button filterBtn = new Button("Filter", e -> updateAbsences(card, user));
        filterLayout.add(startDatePicker, endDatePicker, filterBtn);
        card.add(filterLayout);

        // iniziale popolazione
        updateAbsences(card, user);

        return card;
    }

    private void updateAbsences(VerticalLayout card, UserDTO user) {
        // rimuovo vecchie assenze
        card.getChildren()
                .filter(c -> c instanceof Span)
                .forEach(c -> card.remove(c));

        if (user.getPersonDocumentId() != null) {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            if (start == null || end == null || start.isAfter(end)) {
                NotificationService.showWarning("Invalid date range");
                return;
            }

            List<AbsenceDTO> absences = absenceService.getAllAbsences()
                    .stream()
                    .filter(a -> a.getPersonDocumentId().equals(user.getPersonDocumentId())
                            && !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                    .collect(Collectors.toList());

            if (absences.isEmpty()) {
                card.add(new Span("No absences in selected period"));
            } else {
                absences.forEach(a -> card.add(new Span(a.getDate() + ": " + a.getReason())));
            }
        }
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return Roles.values();
    }
}