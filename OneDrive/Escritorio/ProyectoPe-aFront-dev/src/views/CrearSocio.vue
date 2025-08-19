<template>
  <div class="container formulario-container shadow-sm p-4 rounded mt-5 mb-5">
    <h2 class="form-title">Crear Nuevo Socio</h2>

    <form @submit.prevent="guardarSocio" class="row needs-validation" novalidate>
      <!-- Nombre -->
      <div>
        <label for="nombre" class="form-label">Nombre <span class="text-danger">*</span></label>
        <input id="nombre" v-model.trim="form.nombre" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.nombre }" autocomplete="off" autofocus />
        <div class="invalid-feedback">El nombre es obligatorio.</div>
      </div>

      <!-- Alias -->
      <div>
        <label for="alias" class="form-label">Alias <span class="text-danger">*</span></label>
        <input id="alias" v-model.trim="form.alias" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.alias }" autocomplete="off" />
        <div class="invalid-feedback">El alias es obligatorio.</div>
      </div>

      <div>
        <label for="direccion" class="form-label">Direccion <span class="text-danger">*</span></label>
        <input id="direccion" v-model.trim="form.direccion" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.direccion }" autocomplete="off" />
        <div class="invalid-feedback">La direccion es obligatorio.</div>
      </div>

      <!-- Apellido -->
      <div>
        <label for="apellido" class="form-label">Apellido <span class="text-danger">*</span></label>
        <input id="apellido" v-model.trim="form.apellido" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.apellido }" autocomplete="off" />
        <div class="invalid-feedback">El apellido es obligatorio.</div>
      </div>

      <!-- Email -->
      <div>
        <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
        <input id="email" v-model.trim="form.email" required type="email" class="form-control input-custom"
          :class="{ 'is-invalid': errores.email }" autocomplete="off" />
        <div class="invalid-feedback">Ingrese un email válido.</div>
      </div>

      <!-- DNI -->
      <div>
        <label for="dni" class="form-label">DNI <span class="text-danger">*</span></label>
        <input id="dni" v-model.trim="form.dni" required type="text" class="form-control input-custom"
          :class="{ 'is-invalid': errores.dni }" autocomplete="off" />
        <div class="invalid-feedback">El DNI es obligatorio.</div>
      </div>

      <!-- Num Socio Boca -->
      <div>
        <label for="numSocioBoca" class="form-label">N° Socio Boca</label>
        <input id="numSocioBoca" v-model.number="form.numSocioBoca" type="number" min="0"
          class="form-control input-custom" autocomplete="off" />
      </div>

      <!-- Teléfono -->
      <div>
        <label for="telefono" class="form-label">Teléfono</label>
        <input id="telefono" v-model.trim="form.telefono" type="tel" class="form-control input-custom"
          autocomplete="off" />
      </div>

      <!-- Cobrador -->
      <div>
        <label for="cobradorId" class="form-label">Cobrador <span class="text-danger">*</span></label>
        <select id="cobradorId" v-model="form.cobradorId" required class="form-select input-custom"
          :class="{ 'is-invalid': errores.cobradorId }">
          <option disabled value="">Seleccione un cobrador</option>
          <option v-for="c in cobradores" :key="c.cobradoresId" :value="c.cobradoresId">
            {{ c.nombre }}
          </option>
        </select>
        <div class="invalid-feedback">Seleccione un cobrador.</div>
      </div>

      <!-- Tipo Socio Peña -->
      <div>
        <label for="tipoSocioPeñaId" class="form-label">Tipo Socio Peña <span class="text-danger">*</span></label>
        <select id="tipoSocioPeñaId" v-model="form.tipoSocioPeñaId" required class="form-select input-custom"
          :class="{ 'is-invalid': errores.tipoSocioPeñaId }">
          <option disabled value="">Seleccione un tipo</option>
          <option v-for="t in tiposSocio" :key="t.tipoSocioPeñaId" :value="t.tipoSocioPeñaId">
            {{ t.nombre }}
          </option>
        </select>
        <div class="invalid-feedback">Seleccione un tipo de socio.</div>
      </div>

      <!-- Tipo Boca -->
      <div>
        <label for="tipoBocaId" class="form-label">Tipo Boca <span class="text-danger"></span></label>
        <select id="tipoBocaId" v-model="form.tipoBocaId" required class="form-select input-custom"
          :class="{ 'is-invalid': errores.tipoBocaId }">
          <option disabled value="">Seleccione tipo Boca</option>
          <option v-for="t in tiposBoca" :key="t.tipoSocioBocaId" :value="t.tipoSocioBocaId">
            {{ t.nombre }}
          </option>
        </select>
        <div class="invalid-feedback">Seleccione un tipo Boca.</div>
      </div>

      <!-- Usuario
      <div>
        <label for="userId" class="form-label">Usuario <span class="text-danger">*</span></label>
        <select
          id="userId"
          v-model="form.userId"
          required
          class="form-select input-custom"
          :class="{ 'is-invalid': errores.userId }"
        >
          <option disabled value="">Seleccione usuario</option>
          <option v-for="u in usuarios" :key="u.userId" :value="u.userId">
            {{ u.nombre }}
          </option>
        </select>
        <div class="invalid-feedback">Seleccione un usuario.</div>
      </div> -->

<!-- Localidad (buscador con autocompletado) -->
<!-- Localidad (buscador con autocompletado + server-side) -->
<div class="position-relative">
  <label for="localidadInput" class="form-label">Localidad <span class="text-danger">*</span></label>

  <input
    id="localidadInput"
    v-model="localidadQuery"
      ref="localidadInputEl"   
    type="text"
    class="form-control input-custom"
    :class="{ 'is-invalid': errores.localidadId }"
    placeholder="Escribí para buscar…"
    autocomplete="off"
    @input="onLocalidadInput"
    @keydown.down.prevent="moveHighlight(1)"
    @keydown.up.prevent="moveHighlight(-1)"
    @keydown.enter.prevent="confirmHighlighted()"
    @keydown.esc.prevent="openDropdown = false"
  />

  <!-- Dropdown -->
  <ul
    v-if="openDropdown && localidadQuery.trim().length >= 2 && resultados.length"
    class="dropdown-menu show w-100 shadow-sm typeahead"
    style="max-height: 260px; overflow:auto"
  >
    <li v-for="(l, idx) in resultados" :key="l.localidadId">
      <button
        type="button"
        class="dropdown-item"
        :class="{ active: idx === highlightedIndex }"
        @mousedown.prevent="selectLocalidad(l)"
      >
        {{ l.nombre }} <small v-if="l.provincia">— {{ l.provincia }}</small>
      </button>
    </li>
  </ul>

  <!-- Loading / vacío -->
  <div v-if="openDropdown && localidadQuery.trim().length >= 2 && cargandoLocs" class="small text-muted mt-1">
    Buscando…
  </div>

  <div v-if="openDropdown && localidadQuery.trim().length >= 2 && !cargandoLocs && !resultados.length" class="small text-muted mt-1">
    No se encontraron resultados.
  </div>

  <div class="invalid-feedback">Seleccione una localidad.</div>
</div>


      <!-- Estado -->
      <!-- <div>
        <label for="estado" class="form-label">Estado <span class="text-danger">*</span></label>
        <select id="estado" v-model="form.estado" required class="form-select input-custom">
          <option :value="true">Activo</option>
          <option :value="false">Inactivo</option>
        </select>
      </div> -->

      <!-- Botones -->
      <div class="col-12 d-flex justify-content-center gap-4 mt-5">
        <button type="submit" class="btn btn-boca-primary btn-lg btn-custom" :disabled="cargando" aria-live="polite">
          <span v-if="cargando" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          Guardar
        </button>
        <button type="button" @click="cancelar" class="btn btn-secondary btn-lg btn-custom-cancel" :disabled="cargando">
          Cancelar
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

/* Axios + token (tu API) */
const api = axios.create({ baseURL: 'http://127.0.0.1:8081' })
api.interceptors.request.use((config) => {
  const t = localStorage.getItem('token')
  if (t) config.headers.Authorization = `Bearer ${t}`
  return config
})
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const s = err.response?.status
    if (s === 401) {
      Swal.fire({ icon: 'warning', title: 'Sesión expirada', text: 'Volvé a iniciar sesión.' })
      router.replace('/login')
    } else if (s === 403) {
      Swal.fire({ icon: 'error', title: 'No autorizado', text: 'No tenés permisos para esta acción.' })
    }
    return Promise.reject(err)
  }
)

/* === Cliente Georef (API pública) === */
const georef = axios.create({ baseURL: 'https://apis.datos.gob.ar/georef/api' })

async function buscarLocalidadesGeoref(q, prov = '') {
  const { data } = await georef.get('/localidades', {
    params: {
      nombre: q,
      provincia: prov || undefined,
      max: 50,
      campos: 'id,nombre,provincia.nombre'
    }
  })
  return (data.localidades || []).map(l => ({
    localidadId: null,               // aún no existe en tu BD
    georefId: l.id,                  // opcional para referencia
    nombre: l.nombre,
    provincia: l.provincia?.nombre || '',
    codigoPostal: ''                 // si no lo tenés, lo dejamos vacío
  }))
}

/* Permisos */
function parseJwt(t){ try{const b=t.split('.')[1]?.replace(/-/g,'+').replace(/_/g,'/')||''; return JSON.parse(decodeURIComponent(atob(b).split('').map(c=>'%'+('00'+c.charCodeAt(0).toString(16)).slice(-2)).join('')))}catch{return{}}}
const token = ref(localStorage.getItem('token') || '')
const claims = computed(() => (token.value ? parseJwt(token.value) : {}))
const perms  = computed(() => claims.value.perms || [])
const canManageSocios = computed(() => perms.value.includes('*') || perms.value.includes('socios:gestionar'))

/* Form y catálogos */
const form = ref({
  nombre: '', alias: '', apellido: '', email: '', dni: '',
  numSocioBoca: null, telefono: '', cobradorId: '',
  tipoSocioPeñaId: '', tipoBocaId: '', userId: '',
  localidadId: '', estado: true, direccion: ''
})
const errores = ref({})
const cargando = ref(false)
const cobradores = ref([]), tiposSocio = ref([]), tiposBoca = ref([]), localidades = ref([]), usuarios = ref([])

onMounted(async () => {
  if (!canManageSocios.value) {
    Swal.fire({ icon: 'error', title: 'No autorizado', text: 'No podés crear socios.' })
    router.replace('/socios')
    return
  }
  await Promise.all([cargarCobradores(), cargarTiposSocio(), cargarTiposBoca(), cargarLocalidades(), cargarUsuarios()])
  document.addEventListener('click', onDocumentClick, true)
})

onBeforeUnmount(() => {
  clearTimeout(debounceId)
  if (currentAbort) currentAbort.abort()
  document.removeEventListener('click', onDocumentClick, true)
})

async function cargarCobradores(){ const {data}=await api.get('/cobradores'); cobradores.value=data||[] }
async function cargarTiposSocio(){ const {data}=await api.get('/sociosPeña'); tiposSocio.value=data||[] }
async function cargarTiposBoca(){ const {data}=await api.get('/sociosboca'); tiposBoca.value=data||[] }
async function cargarLocalidades(){ const {data}=await api.get('/localidades'); localidades.value=data||[] }
async function cargarUsuarios(){ try{ const {data}=await api.get('/usuarios'); usuarios.value=data||[] } catch{ usuarios.value=[] } }

function validarFormulario() {
  errores.value = {}
  if (!form.value.nombre?.trim()) errores.value.nombre = true
  if (!form.value.alias?.trim()) errores.value.alias = true
  if (!form.value.direccion?.trim()) errores.value.direccion = true
  if (!form.value.apellido?.trim()) errores.value.apellido = true
  if (!form.value.email || !/\S+@\S+\.\S+/.test(form.value.email)) errores.value.email = true
  if (!form.value.dni?.trim()) errores.value.dni = true
  if (!form.value.cobradorId) errores.value.cobradorId = true
  if (!form.value.tipoSocioPeñaId) errores.value.tipoSocioPeñaId = true
  if (!form.value.localidadId) errores.value.localidadId = true
  return Object.keys(errores.value).length === 0
}

async function guardarSocio() {
  if (!canManageSocios.value) {
    Swal.fire({ icon: 'error', title: 'No autorizado', text: 'No podés crear socios.' })
    return
  }
  if (!validarFormulario()) { window.scrollTo({ top: 0, behavior: 'smooth' }); return }

  cargando.value = true
  try {
    const socioDTO = {
      nombre: form.value.nombre.trim(),
      alias: form.value.alias.trim(),
      direccion: form.value.direccion.trim(),
      apellido: form.value.apellido.trim(),
      email: form.value.email.trim(),
      dni: form.value.dni.trim(),
      numSocioBoca: form.value.numSocioBoca ? Number(form.value.numSocioBoca) : null,
      telefono: form.value.telefono?.trim() || null,
      cobradorId: Number(form.value.cobradorId),
      tipoSocioPeñaId: Number(form.value.tipoSocioPeñaId),
      tipoBocaId: form.value.tipoBocaId ? Number(form.value.tipoBocaId) : null,
      userId: form.value.userId ? Number(form.value.userId) : null,
      localidadId: Number(form.value.localidadId),
      estado: true
    }
    await api.post('/socios', socioDTO)
    Swal.fire('Éxito', 'Socio creado correctamente', 'success')
    router.push('/socios')
  } catch (error) {
    console.error('Error al crear socio:', error)
    const msg = error?.response?.data?.mensaje || error?.response?.data?.error || 'No se pudo crear el socio'
    Swal.fire('Error', msg, 'error')
  } finally {
    cargando.value = false
  }
}

/* ===== Autocomplete Localidades: BD ⇒ fallback Georef ===== */
const localidadQuery = ref('')      // v-model del input
const openDropdown = ref(false)
const highlightedIndex = ref(-1)
const resultados = ref([])
const cargandoLocs = ref(false)
const minLen = 2
let debounceId = null
let currentAbort = null

function onLocalidadInput() {
  const q = localidadQuery.value.trim()
  openDropdown.value = q.length >= minLen
  highlightedIndex.value = 0

  if (!openDropdown.value) {
    resultados.value = []
    cargandoLocs.value = false
    if (currentAbort) { currentAbort.abort(); currentAbort = null }
    return
  }

  clearTimeout(debounceId)
  debounceId = setTimeout(() => buscarLocalidades(q), 250)
}

async function buscarLocalidades(q) {
  try {
    if (currentAbort) currentAbort.abort()
    currentAbort = new AbortController()
    cargandoLocs.value = true

    // 1) Primero tu BD
    const { data } = await api.get('/localidades', { params: { q, limit: 50 }, signal: currentAbort.signal })
    let items = Array.isArray(data)
      ? data.map(l => ({
          localidadId: l.localidadId,
          georefId: null,
          nombre: l.nombre,
          provincia: l.provincia || '',
          codigoPostal: l.codigoPostal || ''
        }))
      : []

    // 2) Si hay pocos resultados, agregamos Georef y mergeamos simple por (nombre+provincia)
    if (items.length < 5) {
      const externos = await buscarLocalidadesGeoref(q)
      const key = x => `${x.nombre}__${x.provincia}`.toLowerCase()
      const set = new Set(items.map(key))
      for (const e of externos) if (!set.has(key(e))) items.push(e)
    }

    resultados.value = items
  } catch (err) {
    if (!(axios.isCancel?.(err) || err?.name === 'CanceledError' || err?.name === 'AbortError')) {
      console.error('Error buscando localidades:', err)
    }
    resultados.value = []
  } finally {
    cargandoLocs.value = false
    currentAbort = null
  }
}

async function selectLocalidad(l) {
  if (l.localidadId) {
    // Ya existe en tu BD
    form.value.localidadId = l.localidadId
  } else {
    // Viene de Georef → crear/obtener en tu BD y quedarnos con el id
    const { data } = await api.post('/localidades/sync', {
      nombre: l.nombre,
      provincia: l.provincia,
      codigoPostal: l.codigoPostal || ''
    })
    form.value.localidadId = data.localidadId
  }

  localidadQuery.value = `${l.nombre}${l.provincia ? ' — ' + l.provincia : ''}`
  openDropdown.value = false
  highlightedIndex.value = -1
  resultados.value = []
  if (errores.value.localidadId) delete errores.value.localidadId
}

function moveHighlight(delta) {
  if (!openDropdown.value || !resultados.value.length) return
  const n = resultados.value.length
  highlightedIndex.value = (((highlightedIndex.value + delta) % n) + n) % n
}

function confirmHighlighted() {
  const l = resultados.value[highlightedIndex.value]
  if (l) selectLocalidad(l)
}

/** Cerrar dropdown clickeando fuera */
function onDocumentClick(e) {
  const el = document.getElementById('localidadInput')
  if (el && !el.contains(e.target)) openDropdown.value = false
}

/* (sin watch duplicado) */
// watch(localidadQuery, ...)

function cancelar() { if (!cargando.value) router.push('/socios') }
</script>



<style scoped>
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
  border-radius: 0.5rem;
  border: 1.8px solid #ced4da;
  padding: 0.5rem 0.75rem;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  font-size: 1rem;
  width: 100%;
}

.input-custom:focus,
.input-custom.is-invalid {
  border-color: #003366 !important;
  box-shadow: 0 0 8px #003366aa !important;
  outline: none;
}

.is-invalid {
  border-color: #dc3545 !important;
  box-shadow: 0 0 8px #dc3545aa !important;
}

.form-label {
  font-weight: 600;
  color: #003366;
  user-select: none;
}

.btn-boca-primary {
  background-color: #003366;
  color: #ffc107;
  border: none;
  border-radius: 30px;
  box-shadow: 0 5px 12px rgb(0 51 102 / 0.4);
  padding: 0.55rem 1.5rem;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.btn-boca-primary:hover,
.btn-boca-primary:focus {
  background-color: #00509e;
  color: #fff;
  outline: none;
  box-shadow: 0 6px 16px rgb(0 80 158 / 0.7);
}

.btn-custom-cancel {
  min-width: 130px;
  border-radius: 30px;
  color: #495057;
  background-color: #e2e6ea;
  box-shadow: none;
  transition: background-color 0.3s ease;
  font-weight: 600;
  padding: 0.55rem 1.5rem;
  font-size: 1.1rem;
}

.btn-custom-cancel:hover {
  background-color: #d3d9df;
  color: #343a40;
}

.spinner-border {
  vertical-align: middle;
  border-width: 0.18em;
}

.text-danger {
  user-select: none;
}

/* dentro de <style scoped> */
.typeahead .dropdown-item.active {
  background: #003366;
  color: #fff;
}

</style>
