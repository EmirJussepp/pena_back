<template>
  <div class="container formulario-container shadow-sm p-4 rounded mt-5 mb-5">
    <h2 class="form-title">Editar Socio</h2>

    <form @submit.prevent="guardarSocio" class="row needs-validation" novalidate>
      <!-- Nombre -->
      <div>
        <label for="nombre" class="form-label">Nombre <span class="text-danger">*</span></label>
        <input id="nombre" v-model.trim="form.nombre" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.nombre }" />
        <div class="invalid-feedback">El nombre es obligatorio.</div>
      </div>

      <!-- Alias -->
      <div>
        <label for="alias" class="form-label">Alias</label>
        <input id="alias" v-model.trim="form.alias" type="text" class="form-control input-custom" />
      </div>

      <!-- Apellido -->
      <div>
        <label for="apellido" class="form-label">Apellido <span class="text-danger">*</span></label>
        <input id="apellido" v-model.trim="form.apellido" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.apellido }" />
        <div class="invalid-feedback">El apellido es obligatorio.</div>
      </div>

      <!-- Dirección -->
      <div>
        <label for="direccion" class="form-label">Direccion <span class="text-danger">*</span></label>
        <input id="direccion" v-model.trim="form.direccion" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.direccion }" />
        <div class="invalid-feedback">La direccion es obligatoria.</div>
      </div>

      <!-- Email -->
      <div>
        <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
        <input id="email" v-model.trim="form.email" required type="email" class="form-control input-custom"
          :class="{ 'is-invalid': errores.email }" />
        <div class="invalid-feedback">Ingrese un email válido.</div>
      </div>

      <!-- DNI -->
      <div>
        <label for="dni" class="form-label">DNI <span class="text-danger">*</span></label>
        <input id="dni" v-model.trim="form.dni" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.dni }" />
        <div class="invalid-feedback">El DNI es obligatorio.</div>
      </div>

      <!-- N° Socio Boca -->
      <div>
        <label for="numSocioBoca" class="form-label">N° Socio Boca</label>
        <input id="numSocioBoca" v-model.number="form.numSocioBoca" type="number" class="form-control input-custom" />
      </div>

      <!-- Teléfono -->
      <div>
        <label for="telefono" class="form-label">Teléfono</label>
        <input id="telefono" v-model.trim="form.telefono" type="tel" class="form-control input-custom" />
      </div>

      <!-- Cobrador -->
      <div>
        <label class="form-label">Cobrador <span class="text-danger">*</span></label>
        <select v-model.number="form.cobradorId" required class="form-select input-custom">
          <option :value="null" disabled>Seleccione un cobrador</option>
          <option v-for="c in cobradores" :key="c.id" :value="c.id">{{ c.nombre }}</option>
        </select>
      </div>

      <!-- Tipo Socio Peña -->
      <div>
        <label class="form-label">Tipo Socio Peña <span class="text-danger">*</span></label>
        <select v-model.number="form.tipoSocioPeñaId" required class="form-select input-custom">
          <option :value="null" disabled>Seleccione tipo</option>
          <option v-for="t in tiposSocio" :key="t.id" :value="t.id">{{ t.nombre }}</option>
        </select>
      </div>

      <!-- Tipo Socio Boca -->
      <div>
        <label class="form-label">Tipo Socio Boca <span class="text-danger">*</span></label>
        <select v-model.number="form.tipoBocaId" class="form-select">
          <option :value="null" disabled>Seleccione tipo Boca</option>
          <option v-for="op in tiposBoca" :key="op.id" :value="op.id">{{ op.nombre }}</option>
        </select>
      </div>

      <!-- Localidad (Autocomplete) -->
      <div class="position-relative">
        <label for="localidadInput" class="form-label">Localidad <span class="text-danger">*</span></label>
        <input id="localidadInput" v-model="localidadQuery" type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.localidadId }" placeholder="Escribí para buscar…" autocomplete="off"
          @input="onLocalidadInput" @keydown.down.prevent="moveHighlight(1)"
          @keydown.up.prevent="moveHighlight(-1)" @keydown.enter.prevent="confirmHighlighted()"
          @keydown.esc.prevent="openDropdown = false" />

        <!-- Dropdown -->
        <ul v-if="openDropdown && localidadQuery.trim().length >= 2 && resultados.length"
          class="dropdown-menu show w-100 shadow-sm typeahead" style="max-height: 260px; overflow:auto">
          <li v-for="(l, idx) in resultados" :key="l.localidadId || l.georefId">
            <button type="button" class="dropdown-item" :class="{ active: idx === highlightedIndex }"
              @mousedown.prevent="selectLocalidad(l)">
              {{ l.nombre }} <small v-if="l.provincia">— {{ l.provincia }}</small>
            </button>
          </li>
        </ul>

        <div v-if="openDropdown && localidadQuery.trim().length >= 2 && cargandoLocs"
          class="small text-muted mt-1">Buscando…</div>
        <div v-if="openDropdown && localidadQuery.trim().length >= 2 && !cargandoLocs && !resultados.length"
          class="small text-muted mt-1">No se encontraron resultados.</div>

        <div class="invalid-feedback">Seleccione una localidad.</div>
      </div>

      <!-- Botones -->
      <div class="col-12 d-flex justify-content-center gap-3 mt-4 flex-wrap">
        <button type="submit" class="btn btn-primary btn-lg" :disabled="cargando">
          <span v-if="cargando" class="spinner-border spinner-border-sm me-2" role="status"></span>
          Actualizar
        </button>
        <button type="button" class="btn btn-secondary btn-lg" @click="router.push('/socios')">Cancelar</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import Swal from "sweetalert2";

/* ====== Rutas y Router ====== */
const route = useRoute();
const router = useRouter();
const socioId = route.params.id;

/* ====== Estado y Formulario ====== */
const cargando = ref(false);
const errores = ref({});
const form = ref({
  socioId: null,
  nombre: "",
  alias: "",
  apellido: "",
  email: "",
  dni: "",
  numSocioBoca: null,
  telefono: "",
  cobradorId: null,
  tipoSocioPeñaId: null,
  tipoBocaId: null,
  userId: null,
  localidadId: null,
  estado: true,
  direccion: "",
});

/* ====== Catálogos ====== */
const cobradores = ref([]);
const tiposSocio = ref([]);
const tiposBoca = ref([]);
const localidades = ref([]);

/* ====== Autocomplete Localidades ====== */
const localidadQuery = ref('');
const openDropdown = ref(false);
const highlightedIndex = ref(-1);
const resultados = ref([]);
const cargandoLocs = ref(false);
let debounceId = null;
let currentAbort = null;
const minLen = 2;

/* ====== Axios con token + manejo 401/403 ====== */
const api = axios.create({ baseURL: "http://127.0.0.1:8081" });
api.interceptors.request.use((config) => {
  const t = localStorage.getItem("token");
  if (t) config.headers.Authorization = `Bearer ${t}`;
  return config;
});
api.interceptors.response.use(
  r => r,
  e => {
    const s = e.response?.status;
    if (s === 401) {
      Swal.fire({ icon: "warning", title: "Sesión expirada", text: "Volvé a iniciar sesión." });
      router.replace("/login");
    } else if (s === 403) {
      Swal.fire({ icon: "error", title: "No autorizado", text: "No tenés permisos para esta acción." });
    }
    return Promise.reject(e);
  }
);

/* ====== Helpers ====== */
function normalize(list, idKeys) {
  return (list || []).map(o => ({
    id: Number(idKeys.reduce((acc, k) => acc ?? o[k], null) ?? o.id),
    nombre: o.nombre,
  }));
}

function findIdByName(list, name) {
  if (!name) return null;
  const needle = String(name).trim().toLowerCase();
  const f = (list || []).find(x => String(x?.nombre || "").trim().toLowerCase() === needle);
  return f ? Number(f.id) : null;
}

/* ====== Autocomplete funciones ====== */
async function buscarLocalidadesGeoref(q) {
  const { data } = await axios.get('https://apis.datos.gob.ar/georef/api/localidades', {
    params: { nombre: q, max: 50, campos: 'id,nombre,provincia.nombre' }
  });
  return (data.localidades || []).map(l => ({
    localidadId: null,
    georefId: l.id,
    nombre: l.nombre,
    provincia: l.provincia?.nombre || '',
    codigoPostal: ''
  }));
}

function onLocalidadInput() {
  const q = localidadQuery.value.trim();
  openDropdown.value = q.length >= minLen;
  highlightedIndex.value = 0;

  if (!openDropdown.value) {
    resultados.value = [];
    cargandoLocs.value = false;
    if (currentAbort) { currentAbort.abort(); currentAbort = null; }
    return;
  }

  clearTimeout(debounceId);
  debounceId = setTimeout(() => buscarLocalidades(q), 250);
}

async function buscarLocalidades(q) {
  try {
    if (currentAbort) currentAbort.abort();
    currentAbort = new AbortController();
    cargandoLocs.value = true;

    const { data } = await api.get('/localidades', { params: { q, limit: 50 }, signal: currentAbort.signal });
    let items = Array.isArray(data) ? data.map(l => ({
      localidadId: l.localidadId,
      georefId: null,
      nombre: l.nombre,
      provincia: l.provincia || '',
      codigoPostal: l.codigoPostal || ''
    })) : [];

    if (items.length < 5) {
      const externos = await buscarLocalidadesGeoref(q);
      const key = x => `${x.nombre}__${x.provincia}`.toLowerCase();
      const set = new Set(items.map(key));
      for (const e of externos) if (!set.has(key(e))) items.push(e);
    }

    resultados.value = items;
  } catch {
    resultados.value = [];
  } finally {
    cargandoLocs.value = false;
    currentAbort = null;
  }
}

async function selectLocalidad(l) {
  if (l.localidadId) {
    form.value.localidadId = l.localidadId;
  } else {
    const { data } = await api.post('/localidades/sync', {
      nombre: l.nombre,
      provincia: l.provincia,
      codigoPostal: l.codigoPostal || ''
    });
    form.value.localidadId = data.localidadId;
  }

  localidadQuery.value = `${l.nombre}${l.provincia ? ' — ' + l.provincia : ''}`;
  openDropdown.value = false;
  highlightedIndex.value = -1;
  resultados.value = [];
  if (errores.value.localidadId) delete errores.value.localidadId;
}

function moveHighlight(delta) {
  if (!openDropdown.value || !resultados.value.length) return;
  const n = resultados.value.length;
  highlightedIndex.value = (((highlightedIndex.value + delta) % n) + n) % n;
}

function confirmHighlighted() {
  const l = resultados.value[highlightedIndex.value];
  if (l) selectLocalidad(l);
}

function onDocumentClick(e) {
  const el = document.getElementById('localidadInput');
  if (el && !el.contains(e.target)) openDropdown.value = false;
}

/* ====== Carga inicial ====== */
onMounted(async () => {
  await Promise.all([cargarCobradores(), cargarTiposSocio(), cargarTiposBoca(), cargarLocalidades()]);
  await cargarSocioExistente();
});

/* ====== Carga socio existente ====== */
async function cargarSocioExistente() {
  try {
    const { data: socio } = await api.get(`/socios/${socioId}`);

    // Campos planos
    form.value.socioId = Number(socio.socioId);
    form.value.nombre = socio.nombre || "";
    form.value.alias = socio.alias || "";
    form.value.apellido = socio.apellido || "";
    form.value.email = socio.email || "";
    form.value.dni = socio.dni || "";
    form.value.numSocioBoca = socio.numSocioBoca != null ? Number(socio.numSocioBoca) : null;
    form.value.telefono = socio.telefono || "";
    form.value.estado = socio.estado ?? true;
    form.value.direccion = socio.direccion || "";

    // IDs preferidos
    form.value.cobradorId = socio.cobradorId != null ? Number(socio.cobradorId) : findIdByName(cobradores.value, socio.cobradorNombre);
    form.value.tipoSocioPeñaId = (socio.tipoPenaId ?? socio.tipoSocioPeñaId) != null ? Number(socio.tipoPenaId ?? socio.tipoSocioPeñaId) : findIdByName(tiposSocio.value, socio.tipoPeñaNombre ?? socio.tipoPenaNombre);
    form.value.tipoBocaId = socio.tipoBocaId != null ? Number(socio.tipoBocaId) : findIdByName(tiposBoca.value, socio.tipoBocaNombre);
    form.value.localidadId = socio.localidadId != null ? Number(socio.localidadId) : findIdByName(localidades.value, socio.localidadNombre);

    // Precargar texto del input localidad
    const loc = localidades.value.find(l => l.id === form.value.localidadId);
    localidadQuery.value = loc ? loc.nombre : socio.localidadNombre || '';

    // userId si viene
    form.value.userId = socio.userId != null ? Number(socio.userId) : null;
  } catch (error) {
    console.error("Error al cargar el socio:", error);
    Swal.fire("Error", "No se pudo cargar el socio", "error");
    router.push("/socios");
  }
}

/* ====== Validación ====== */
function validarFormulario() {
  errores.value = {};
  if (!form.value.nombre?.trim()) errores.value.nombre = true;
  if (!form.value.apellido?.trim()) errores.value.apellido = true;
  if (!form.value.direccion?.trim()) errores.value.direccion = true;
  if (!form.value.email || !/\S+@\S+\.\S+/.test(form.value.email)) errores.value.email = true;
  if (!form.value.dni?.trim()) errores.value.dni = true;
  if (!form.value.cobradorId) errores.value.cobradorId = true;
  if (!form.value.tipoSocioPeñaId) errores.value.tipoSocioPeñaId = true;
  if (!form.value.localidadId) errores.value.localidadId = true;
  return Object.keys(errores.value).length === 0;
}

/* ====== Guardar (PATCH) ====== */
async function guardarSocio() {
  if (!validarFormulario()) {
    window.scrollTo({ top: 0, behavior: "smooth" });
    return;
  }

  cargando.value = true;
  try {
    const socioDTO = {
      socioId: Number(socioId),
      nombre: form.value.nombre.trim(),
      direccion: form.value.direccion.trim(),
      alias: form.value.alias?.trim() || null,
      apellido: form.value.apellido.trim(),
      email: form.value.email.trim(),
      dni: form.value.dni.trim(),
      numSocioBoca: form.value.numSocioBoca != null ? Number(form.value.numSocioBoca) : null,
      telefono: form.value.telefono?.trim() || null,
      cobradorId: Number(form.value.cobradorId),
      tipoPenaId: Number(form.value.tipoSocioPeñaId),
      tipoBocaId: form.value.tipoBocaId != null ? Number(form.value.tipoBocaId) : null,
      userId: form.value.userId != null ? Number(form.value.userId) : null,
      localidadId: Number(form.value.localidadId),
      estado: true,
    };

    console.log("📤 Enviando DTO al backend:", socioDTO);
    await api.patch(`/socios/${socioId}`, socioDTO);
    Swal.fire("Éxito", "Socio actualizado correctamente", "success");
    router.push("/socios");
  } catch (error) {
    console.error("Error al actualizar socio:", error);
    const msg = error.response?.data?.error || "No se pudo actualizar el socio";
    Swal.fire("Error", msg, "error");
  } finally {
    cargando.value = false;
  }
}

/* ====== Cargar catálogos ====== */
async function cargarCobradores() {
  const { data } = await api.get("/cobradores");
  cobradores.value = normalize(data, ["cobradorId", "cobradoresId", "cobrador_id", "id"]);
}

async function cargarTiposSocio() {
  const { data } = await api.get("/sociosPeña");
  tiposSocio.value = normalize(data, ["tipoPenaId", "tipoSocioPeñaId", "tipo_id_socioPeña", "id"]);
}

async function cargarTiposBoca() {
  const { data } = await api.get("/sociosboca");
  tiposBoca.value = normalize(data, ["tipoBocaId", "tipoSocioBocaId", "tipo_boca_id", "id"]);
}

async function cargarLocalidades() {
  const { data } = await api.get("/localidades");
  localidades.value = normalize(data, ["localidadId", "localidad_id", "id"]);
}
</script>

<style scoped>
/* --- Contenedor del formulario --- */
.formulario-container {
  max-width: 950px;
  margin-left: auto;
  margin-right: auto;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgb(0 51 102 / 0.15);
  user-select: none;
  padding: 2rem 2.5rem;
}

.form-title {
  font-weight: 900;
  font-size: 2rem;
  color: #003366;
  text-align: center;
  margin-bottom: 1rem;
  position: relative;
  user-select: none;
}

.form-title::after {
  content: "";
  display: block;
  width: 120px;
  height: 3.5px;
  background: #ffc107;
  margin: 0.25rem auto 0;
  border-radius: 2px;
}

form.row {
  display: flex;
  flex-wrap: wrap;
  row-gap: 1.5rem;
  column-gap: 1.5rem;
}

form.row>div {
  flex: 0 0 100%;
  max-width: 100%;
}

@media (min-width: 768px) {
  form.row>div {
    flex: 0 0 48%;
    max-width: 48%;
  }
}

@media (min-width: 1200px) {
  form.row>div {
    flex: 0 0 30%;
    max-width: 30%;
  }
}
.input-custom {
  font-size: 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 6px rgb(0 0 0 / 0.05);
  border: 1px solid #ced4da;
  padding: 0.5rem 0.75rem;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.input-custom:focus {
  border-color: #00509e;
  box-shadow: 0 0 0 0.2rem rgba(0, 80, 158, 0.25);
  outline: none;
}

.is-invalid {
  border-color: #dc3545 !important;
  box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
}

/* --- Selects --- */
select.input-custom {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 16 16' fill='%23336699' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1.5 5l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 1rem;
  padding-right: 2rem;
}

/* --- Botones --- */
button.btn {
  font-size: 1rem;
  padding: 0.55rem 1.25rem;
  font-weight: 700;
  border-radius: 0.5rem;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #003366;
  border-color: #003366;
  color: #ffc107;
}

.btn-primary:hover,
.btn-primary:focus {
  background-color: #00509e;
  border-color: #00509e;
  color: #fff;
}

.btn-secondary {
  background-color: #6c757d;
  border-color: #6c757d;
  color: #fff;
}

.btn-secondary:hover {
  background-color: #5a6268;
  border-color: #545b62;
}

/* --- Spinner --- */
.spinner-border {
  vertical-align: middle;
}

/* --- Feedback de validación --- */
.invalid-feedback {
  font-size: 0.875rem;
  color: #dc3545;
  margin-top: 0.25rem;
}

/* --- Responsive (extra) --- */
@media (min-width: 768px) {
  .formulario-container {
    max-width: 700px;
    margin: auto;
  }
}
</style>