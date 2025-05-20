import {
    Button,
    ComboBox,
    Dialog,
    Grid,
    GridColumn,
    Notification,
    TextField,
    VerticalLayout,
} from '@vaadin/react-components';
import { useEffect, useState } from 'react';
import handleError from 'Frontend/views/_ErrorHandler';
import { Group, ViewToolbar } from 'Frontend/components/ViewToolbar';
import { useDataProvider } from '@vaadin/hilla-react-crud';

import { CancionService, AlbumService, GeneroService } from '../generated/endpoints';

import Cancion from '../generated/tercero/com/base/models/Cancion';
import TipoArchivoEnum from '../generated/tercero/com/base/models/TipoArchivoEnum';

import Album from 'Frontend/generated/tercero/com/base/models/Album';
import Genero from 'Frontend/generated/tercero/com/base/models/Genero';

const labelsMap: Record<string, string> = {
    FISICO: 'Físico',
    VIRTUAL: 'Virtual',
};

function isValidNumber(value: string) {
    const num = Number(value);
    return !isNaN(num) && num > 0;
}

type CancionEntryFormProps = {
    onCancionCreated?: () => void;
};

function CancionEntryForm({ onCancionCreated }: CancionEntryFormProps) {
    const [dialogOpened, setDialogOpened] = useState(false);

    const [albums, setAlbums] = useState<Album[]>([]);
    const [generos, setGeneros] = useState<Genero[]>([]);
    const [tipos, setTipos] = useState<{ label: string; value: string }[]>([]);

    const [nombre, setNombre] = useState('');
    const [duracion, setDuracion] = useState('');
    const [tipo, setTipo] = useState<string | undefined>(undefined);
    const [albumId, setAlbumId] = useState<string | undefined>(undefined);
    const [generoId, setGeneroId] = useState<string | undefined>(undefined);

    useEffect(() => {
        AlbumService.lisAllAlbum()
            .then((data) => setAlbums((data ?? []).filter((item): item is Album => !!item)))
            .catch(handleError);

        GeneroService.lisAllGenero()
            .then((data) => setGeneros((data ?? []).filter((item): item is Genero => !!item)))
            .catch(handleError);

        const tiposLista = Object.entries(TipoArchivoEnum).map(([key]) => ({
            label: labelsMap[key] ?? key,
            value: key,
        }));
        setTipos(tiposLista);
    }, []);

    const open = () => setDialogOpened(true);
    const close = () => setDialogOpened(false);

    const createCancion = async () => {
        if (!nombre.trim() || !duracion.trim() || !tipo || !albumId || !generoId) {
            Notification.show('Faltan datos para crear la canción', {
                duration: 5000,
                position: 'top-center',
                theme: 'error',
            });
            return;
        }

        if (!isValidNumber(duracion)) {
            Notification.show('Duración inválida', {
                duration: 4000,
                position: 'top-center',
                theme: 'error',
            });
            return;
        }

        try {
            await CancionService.createCancion(
                nombre.trim(),
                duracion.trim(),
                tipo,
                albumId,
                generoId
            );

            onCancionCreated?.();

            setNombre('');
            setDuracion('');
            setTipo(undefined);
            setAlbumId(undefined);
            setGeneroId(undefined);
            setDialogOpened(false);

            Notification.show('Canción creada exitosamente', {
                duration: 5000,
                position: 'bottom-end',
                theme: 'success',
            });
        } catch (error) {
            handleError(error);
        }
    };

    return (
        <>
            <Dialog
                aria-label="Registrar Canción"
                draggable
                modeless
                opened={dialogOpened}
                onOpenedChanged={(e) => setDialogOpened(e.detail.value)}
                header={
                    <h2
                        className="draggable"
                        style={{
                            flex: 1,
                            cursor: 'move',
                            margin: 0,
                            fontSize: '1.5em',
                            fontWeight: 'bold',
                            padding: 'var(--lumo-space-m) 0',
                        }}
                    >
                        Registrar Canción
                    </h2>
                }
                footerRenderer={() => (
                    <>
                        <Button onClick={close}>Cancelar</Button>
                        <Button theme="primary" onClick={createCancion}>
                            Registrar
                        </Button>
                    </>
                )}
            >
                <VerticalLayout
                    theme="spacing"
                    style={{ width: '300px', maxWidth: '100%', alignItems: 'stretch' }}
                >
                    <TextField
                        label="Nombre"
                        placeholder="Ingrese el nombre de la canción"
                        aria-label="Ingrese el nombre de la canción"
                        value={nombre}
                        onValueChanged={(e) => setNombre(e.detail.value)}
                    />
                    <TextField
                        label="Duración (segundos)"
                        placeholder="Ingrese la duración de la canción"
                        aria-label="Ingrese la duración de la canción"
                        value={duracion}
                        onValueChanged={(e) => setDuracion(e.detail.value)}
                    />
                    <ComboBox
                        label="Tipo de archivo"
                        items={tipos}
                        itemLabelPath="label"
                        itemValuePath="value"
                        value={tipo}
                        onValueChanged={(e) => setTipo(e.detail.value)}
                        placeholder="Seleccione tipo de archivo"
                    />
                    <ComboBox
                        label="Álbum"
                        items={albums}
                        itemLabelPath="nombre"
                        itemValuePath="id"
                        value={albumId}
                        onValueChanged={(e) => setAlbumId(e.detail.value)}
                        placeholder="Seleccione un álbum"
                    />
                    <ComboBox
                        label="Género"
                        items={generos}
                        itemLabelPath="nombre"
                        itemValuePath="id"
                        value={generoId}
                        onValueChanged={(e) => setGeneroId(e.detail.value)}
                        placeholder="Seleccione un género"
                    />
                </VerticalLayout>
            </Dialog>

            <Button onClick={open}>Registrar</Button>
        </>
    );
}

function tipoArchivoRenderer({ item }: { item: Cancion }) {
    if (!item.tipo) return null;
    return <span>{labelsMap[item.tipo] ?? item.tipo}</span>;
}

export default function CancionListView() {
    const dataProvider = useDataProvider<Cancion>({
        list: async () => {
            try {
                const result = await CancionService.listAllCancion();
                return (result ?? []).filter((c): c is Cancion => c !== undefined && c !== null);
            } catch (error) {
                Notification.show('Error cargando canciones', {
                    theme: 'error',
                    position: 'top-center',
                    duration: 4000,
                });
                return [];
            }
        },
    });

    const urlRenderer = ({ item }: { item: Cancion }) =>
        item.url ? (
            <a href={`/cancion/${item.id}`} style={{ color: 'var(--lumo-primary-text-color)' }}>
                {item.url}
            </a>
        ) : null;

    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <ViewToolbar title="Canciones">
                <Group>
                    <CancionEntryForm onCancionCreated={() => dataProvider.refresh()} />
                </Group>
            </ViewToolbar>

            <Grid dataProvider={dataProvider.dataProvider}>
                <GridColumn path="nombre" header="Nombre" />
                <GridColumn
                    header="Duración"
                    renderer={({ item }: { item: Cancion }) => (
                        <span>{item.duracion != null ? formatDuracion(item.duracion) : '00:00'}</span>
                    )}
                />
                <GridColumn header="URL" renderer={urlRenderer} />
                <GridColumn header="Tipo de Archivo" renderer={tipoArchivoRenderer} />
            </Grid>
        </main>
    );
}

function formatDuracion(segundos: number | string): string {
    const s = typeof segundos === 'string' ? parseInt(segundos) : segundos;
    if (isNaN(s)) return '00:00';
    const minutos = Math.floor(s / 60);
    const segundosRestantes = s % 60;
    return `${String(minutos).padStart(2, '0')}:${String(segundosRestantes).padStart(2, '0')}`;
}
