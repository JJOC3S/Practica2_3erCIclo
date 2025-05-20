package tercero.com.base.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import tercero.com.base.controller.services.CancionService;
import tercero.com.base.models.TipoArchivoEnum;

import java.util.stream.Stream;


@PageTitle("Detalle Canción")
public class CancionDetailView extends Main implements HasUrlParameter<Integer> {

    private final CancionService cancionService;

    // Controles del formulario
    private final TextField nombre = new TextField("Nombre");
    private final NumberField duracion = new NumberField("Duración (segundos)");
    private final TextField url = new TextField("URL");
    private final ComboBox<String> tipo = new ComboBox<>("Tipo");

    // Contenedor para mostrar los datos en modo lectura
    private final VerticalLayout tarjetaDatos = new VerticalLayout();

    private Integer id;

    public CancionDetailView(CancionService cancionService) {
        System.out.println("1 - Constructor CancionDetailView");
        this.cancionService = cancionService;

        tipo.setItems(Stream.of(TipoArchivoEnum.values()).map(Enum::name).toList());
        tipo.setPlaceholder("Tipo de archivo");

        Button guardarBtn = new Button("Guardar");
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarBtn.addClickListener(e -> {
            System.out.println("2 - Click en Guardar");
            guardarCambios();
        });

        // Contenedor formulario
        VerticalLayout formularioLayout = new VerticalLayout(nombre, duracion, url, tipo, guardarBtn);
        formularioLayout.setPadding(true);  // <-- padding para separar contenido del borde
        formularioLayout.setSpacing(true);
        formularioLayout.setWidth("400px");  // <-- ancho fijo para que no se estire

        // Estilo para la tarjeta de datos (info en modo lectura)
        tarjetaDatos.add(new H3("Información de la Canción"));
        tarjetaDatos.getStyle().set("border", "1px solid #ccc"); // borde para separar visualmente
        tarjetaDatos.getStyle().set("padding", "1rem");
        tarjetaDatos.setWidth("400px");  // mismo ancho que formulario para alinear
        tarjetaDatos.setSpacing(true);

        // Contenedor principal para centrar contenido y separar secciones
        VerticalLayout mainLayout = new VerticalLayout(tarjetaDatos, formularioLayout);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);  // centra horizontalmente
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);
        mainLayout.setWidthFull();

        add(mainLayout);
    }

    private void cargarDatos() throws Exception {
        System.out.println("3 - Iniciando cargarDatos() para id: " + id);

        if (cancionService.getCancionById(id) == null) {
            System.out.println("4 - Canción con id " + id + " NO encontrada");
            Notification.show("Canción no encontrada", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        System.out.println("5 - Canción encontrada, cargando datos");
        cancionService.setCancion(cancionService.getCancionById(id));
        System.out.println("6 - Datos cargados: ID: " + cancionService.getCancion().getId() +
                " Nombre: " + cancionService.getCancion().getNombre());

        tarjetaDatos.removeAll();
        tarjetaDatos.add(new H3("Información de la Canción"));
        tarjetaDatos.add(new Paragraph("Nombre: " + (cancionService.getCancion().getNombre() != null ? cancionService.getCancion().getNombre() : "")));
        tarjetaDatos.add(new Paragraph("Duración (segundos): " + (cancionService.getCancion().getDuracion() != null ? cancionService.getCancion().getDuracion() : "")));
        tarjetaDatos.add(new Paragraph("URL: " + (cancionService.getCancion().getUrl() != null ? cancionService.getCancion().getUrl() : "")));
        tarjetaDatos.add(new Paragraph("Tipo: " + (cancionService.getCancion().getTipo() != null ? cancionService.getCancion().getTipo().name() : "")));

        nombre.setValue(cancionService.getCancion().getNombre() != null ? cancionService.getCancion().getNombre() : "");
        duracion.setValue(cancionService.getCancion().getDuracion() != null ? Double.valueOf(cancionService.getCancion().getDuracion()) : 0);
        url.setValue(cancionService.getCancion().getUrl() != null ? cancionService.getCancion().getUrl() : "");
        tipo.setValue(cancionService.getCancion().getTipo() != null ? cancionService.getCancion().getTipo().name() : "");

        System.out.println("7 - Datos cargados en formulario");
    }

    private void guardarCambios() {
        System.out.println("8 - Iniciando guardarCambios()");
        if (nombre.isEmpty() || duracion.isEmpty() || url.isEmpty() || tipo.isEmpty()) {
            System.out.println("9 - Campos incompletos");
            Notification.show("Complete todos los campos", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            System.out.println("10 - Validando tipo de archivo");
            TipoArchivoEnum tipoEnum = TipoArchivoEnum.valueOf(tipo.getValue());

            cancionService.updateCancion(
                    id,
                    nombre.getValue(),
                    String.valueOf(duracion.getValue().intValue()),
                    url.getValue(),
                    tipoEnum
            );

            System.out.println("11 - Canción actualizada correctamente");

            Notification.show("Canción actualizada correctamente", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            cargarDatos();

        } catch (Exception e) {
            System.out.println("12 - Error al guardar: " + e.getMessage());
            Notification.show("Error al guardar: " + e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer integer) {
        System.out.println("13 - setParameter llamado con id: " + integer);
        this.id = integer;
        try {
            cargarDatos();
        } catch (Exception e) {
            System.out.println("14 - Error en cargarDatos dentro de setParameter: " + e.getMessage());
            Notification.show("Error al cargar los datos", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
