<template>
  <div class="container my-5">
    <!-- Título -->
    <div class="page-header mb-4 text-center">
      <h1>Listado de Viajes Oficiales</h1>
      <p class="text-muted mb-0">Gestioná pasajeros y pagos de cada viaje de forma simple.</p>
    </div>

    <!-- Filtros -->
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap gap-3 justify-content-between align-items-end">
        <div class="flex-grow-1">
          <label class="form-label">Buscar por destino o fecha</label>
          <div class="position-relative">
            <i class="bi bi-search position-absolute" style="left:10px; top: 10px;"></i>
            <input v-model="filtroBusqueda" type="text" class="form-control ps-5" placeholder="Ej: River, 06/2025"
              @input="onFilterInput" />
            <button v-if="filtroBusqueda" class="btn btn-sm btn-link position-absolute end-0 top-0 mt-1 me-1"
              @click="limpiarFiltro">Limpiar</button>
          </div>
          <small class="text-muted">Tip: también podés escribir 09/2025 para filtrar por mes/año.</small>
        </div>
      </div>
    </div>

    <!-- Lista de viajes -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div v-if="isLoadingViajes" class="p-4">
          <div class="skeleton mb-3" style="height:50px"></div>
          <div class="skeleton mb-3" style="height:50px"></div>
          <div class="skeleton" style="height:50px"></div>
        </div>

        <div v-else-if="viajes.length === 0" class="text-center text-muted p-5">
          No hay viajes registrados.
        </div>

        <div v-else>
          <div v-for="viaje in viajes" :key="viaje.viajeBomboneraId"
            class="card mb-0 shadow-sm rounded-0 border-0 border-bottom">
            <!-- Encabezado del viaje -->
            <button class="viaje-header w-100 text-start" @click="togglePagos(viaje.viajeBomboneraId)"
              :aria-expanded="viajeSeleccionado === viaje.viajeBomboneraId">
              <div class="left">
                <span class="date-badge">{{ formatFecha(viaje.fechaViaje) }}</span>
                <h3 class="destino mb-0">{{ viaje.destino }}</h3>
                <div class="meta">
                  <span class="chip">
                    <i class="bi bi-people me-1"></i>
                    {{ pasajerosPorViaje[viaje.viajeBomboneraId] || 0 }} pasajeros
                  </span>
                  <span class="chip success">
                    <i class="bi bi-cash-coin me-1"></i>
                    $ {{ formatoMoneda(totalPorViaje[viaje.viajeBomboneraId] || 0) }}
                  </span>
                </div>
              </div>
              <div class="right d-flex align-items-center gap-2">
                <span class="ver">
                  {{ viajeSeleccionado === viaje.viajeBomboneraId ? 'Ocultar detalles' : 'Ver detalles' }}
                </span>
                <i class="bi"
                  :class="viajeSeleccionado === viaje.viajeBomboneraId ? 'bi-chevron-up' : 'bi-chevron-down'"></i>

                <!-- Botón editar viaje -->
                <button @click.stop="editarViaje(viaje)" class="btn btn-sm btn-outline-primary ms-2"
                  title="Editar viaje">
                  <i class="bi bi-pencil"></i>
                </button>
              </div>

            </button>

            <!-- Contenido expandible -->
            <transition name="fade">
              <div v-if="viajeSeleccionado === viaje.viajeBomboneraId" class="card-body">
                <div v-if="isLoadingPagos" class="p-3">
                  <div class="skeleton mb-2" style="height:36px"></div>
                  <div class="skeleton mb-2" style="height:36px"></div>
                  <div class="skeleton" style="height:36px"></div>
                </div>

                <template v-else>
                  <!-- Tabs -->
                  <ul class="nav nav-tabs mb-4 boca-tabs sticky-tabs">
                    <li class="nav-item">
                      <button class="nav-link" :class="{ active: activeTab === 'lista' }"
                        @click="activeTab = 'lista'">Pagos registrados</button>
                    </li>
                    <li class="nav-item">
                      <button class="nav-link" :class="{ active: activeTab === 'registro' }"
                        @click="activeTab = 'registro'">{{ modoEdicion ? 'Editar pago' : 'Registrar nuevo pago'
                        }}</button>
                    </li>
                  </ul>

                  <!-- Resumen rápido del viaje -->
                  <div class="row g-3 mb-3" v-if="activeTab === 'lista'">
                    <div class="col-md-4">
                      <div class="mini-card">
                        <div class="label">Pasajeros</div>
                        <div class="value">{{ pagos.length }}</div>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="mini-card">
                        <div class="label">Total recaudado</div>
                        <div class="value text-success">$ {{ formatoMoneda(totalPagos) }}</div>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="mini-card">
                        <div class="label">Métodos usados</div>
                        <div class="value">{{ Object.keys(agrupadoPorMetodo).length }}</div>
                      </div>
                    </div>
                  </div>

                  <!-- Tab: Lista -->
                  <div v-if="activeTab === 'lista'">
                    <div class="d-flex flex-wrap gap-2 align-items-center mb-2">
                      <span class="fw-semibold">Filtrar lista:</span>
                      <input v-model="filtroPagos" class="form-control form-control-sm w-auto"
                        placeholder="Nombre, DNI…" />
                      <select v-model="ordenPagos" class="form-select form-select-sm w-auto">
                        <option value="apellido">Apellido</option>
                        <option value="nombre">Nombre</option>
                        <option value="montoDesc">Monto (↓)</option>
                        <option value="montoAsc">Monto (↑)</option>
                      </select>
                    </div>

                    <div class="table-responsive scroll-pagos">
                      <table class="table align-middle table-hover mb-0">
                        <thead class="table-light sticky-top">
                          <tr>
                            <th>Pasajero</th>
                            <th class="text-nowrap">DNI</th>
                            <th class="text-nowrap">Método</th>
                            <th class="text-nowrap">Cobrador</th>
                            <th class="text-end">Monto</th>
                            <th class="text-end">Acciones</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="p in pagosFiltradosOrdenados" :key="p.viajePagoId">
                            <td class="fw-semibold">{{ p.nombre }} {{ p.apellido }}</td>
                            <td class="text-muted">{{ p.dni }}</td>
                            <td>
                              <span class="badge bg-secondary-subtle text-dark border">{{ p.metodoPagoNombre }}</span>
                            </td>
                            <td class="text-muted">{{ p.cobradorNombre }}</td>
                            <td class="text-end text-success fw-semibold">$ {{ formatoMoneda(parseFloat(p.monto)) }}
                            </td>
                            <td class="text-end">
                              <button @click="editarPago(p)" class="btn btn-sm btn-outline-primary me-2" title="Editar">
                                <i class="bi bi-pencil"></i>
                              </button>
                              <button @click="eliminarPago(p.viajePagoId)" class="btn btn-sm btn-outline-danger"
                                title="Eliminar">
                                <i class="bi bi-trash"></i>
                              </button>
                            </td>
                          </tr>
                          <tr v-if="pagosFiltradosOrdenados.length === 0">
                            <td colspan="6" class="text-center text-muted py-3">No hay pagos para mostrar.</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>

                    <!-- Resumen por método -->
                    <div class="mt-3">
                      <span class="fw-semibold me-2">Resumen por método:</span>
                      <span v-for="(total, metodo) in agrupadoPorMetodo" :key="metodo"
                        class="badge bg-light text-dark border me-2">
                        {{ metodo }}: $ {{ formatoMoneda(total) }}
                      </span>
                    </div>
                  </div>

                  <!-- Tab: Registro / Edición -->
                  <div v-else>
                    <form @submit.prevent="enviarPago" class="needs-validation" novalidate @keydown.enter.prevent>
                      <div class="row g-3 mb-3">
                        <div class="col-md-6">
                          <label for="nombre" class="form-label fw-semibold text-secondary">Nombre</label>
                          <input v-model.trim="pago.nombre" id="nombre" type="text" class="form-control shadow-sm"
                            placeholder="Ingrese nombre" required />
                        </div>
                        <div class="col-md-6">
                          <label for="apellido" class="form-label fw-semibold text-secondary">Apellido</label>
                          <input v-model.trim="pago.apellido" id="apellido" type="text" class="form-control shadow-sm"
                            placeholder="Ingrese apellido" required />
                        </div>
                      </div>

                      <div class="row g-3 mb-3">
                        <div class="col-md-6">
                          <label for="dni" class="form-label fw-semibold text-secondary">DNI</label>
                          <input v-model="pago.dni" id="dni" type="text" inputmode="numeric" pattern="[0-9]*"
                            class="form-control shadow-sm" placeholder="Sólo números" @input="soloNumeros('dni')"
                            required />
                        </div>
                        <div class="col-md-6">
                          <label for="monto" class="form-label fw-semibold text-secondary">Monto</label>
                          <div class="input-group shadow-sm">
                            <span class="input-group-text">$</span>
                            <input v-model.number="pago.monto" id="monto" type="number" min="0" step="0.01"
                              class="form-control" placeholder="0,00" required />
                          </div>
                        </div>
                      </div>

                      <div class="row g-3 mb-4">
                        <div class="col-md-6">
                          <label for="metodoPago" class="form-label fw-semibold text-secondary">Método de Pago</label>
                          <select v-model="pago.metodoPagoId" id="metodoPago" class="form-select shadow-sm" required>
                            <option disabled value="">Seleccione método de pago</option>
                            <option v-for="metodo in metodosPago" :key="metodo.metodoPagoId"
                              :value="metodo.metodoPagoId">
                              {{ metodo.nombre }}
                            </option>
                          </select>
                        </div>
                        <div class="col-md-6">
                          <label for="cobrador" class="form-label fw-semibold text-secondary">Cobrador</label>
                          <select id="cobrador" class="form-select shadow-sm" v-model="pago.cobradorId" required>
                            <option disabled value="">Seleccione cobrador</option>
                            <option v-for="c in cobradores" :key="c.cobradoresId" :value="c.cobradoresId">
                              {{ c.nombre }}
                            </option>
                          </select>
                        </div>
                      </div>

                      <button type="submit" class="btn-boca-primary w-100 shadow-sm" :disabled="enviando">
                        <span v-if="enviando" class="spinner-border spinner-border-sm me-2" role="status"
                          aria-hidden="true"></span>
                        {{ modoEdicion ? 'Actualizar Pago' : 'Registrar Pago' }}
                      </button>

                      <button v-if="modoEdicion" type="button" class="btn btn-outline-secondary w-100 mt-2"
                        @click="cancelarEdicion">Cancelar edición</button>
                    </form>
                  </div>
                </template>
              </div>
            </transition>
            <!-- Formulario de edición de viaje -->
            <div v-if="modoEdicionViaje && viajeForm.viajeBomboneraId === viaje.viajeBomboneraId"
              class="card-body border-top">
              <form @submit.prevent="actualizarViaje">
                <div class="row g-3 mb-3">
                  <div class="col-md-6">
                    <label class="form-label">Destino</label>
                    <input v-model="viajeForm.destino" type="text" class="form-control" required />
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Fecha del viaje</label>
                    <input v-model="viajeForm.fechaViaje" type="date" class="form-control" required />
                  </div>
                </div>
                <div class="d-flex gap-2">
                  <button type="submit" class="btn btn-primary">Actualizar viaje</button>
                  <button type="button" class="btn btn-outline-secondary"
                    @click="modoEdicionViaje = false">Cancelar</button>
                </div>
              </form>
            </div>

          </div>

          <!-- Paginación -->
          <div v-if="totalPaginas > 1" class="d-flex justify-content-center gap-2 py-3 flex-wrap">
            <button class="btn btn-outline-boca" :disabled="paginaActual === 1" @click="paginaActual--">
              ← Anterior
            </button>

            <button v-for="n in totalPaginas" :key="n" class="btn btn-outline-boca"
              :class="{ 'btn-boca-primary': n === paginaActual }" @click="paginaActual = n">
              {{ n }}
            </button>

            <button class="btn btn-outline-boca" :disabled="paginaActual === totalPaginas" @click="paginaActual++">
              Siguiente →
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'

/* ==========================
   ESTADO / REACTIVOS
========================== */
const viajes = ref([])
const pagosPorViaje = reactive({})
const metodosPago = ref([])
const cobradores = ref([])

const viajeSeleccionado = ref(null)
const activeTab = ref('lista')
const paginaActual = ref(1)
const porPagina = 5
const totalPaginas = ref(1)

const filtroBusqueda = ref('')
const filtroMes = ref('')
const orden = ref('fechaDesc')

const filtroPagos = ref('')
const ordenPagos = ref('apellido')

const modoEdicion = ref(false)
const enviando = ref(false)

const isLoadingViajes = ref(true)
const isLoadingPagos = ref(false)

const viajeForm = reactive({
  fechaViaje: '',
  destino: ''
})

const modoEdicionViaje = ref(false)

const editarViaje = (v) => {
  viajeForm.viajeBomboneraId = v.viajeBomboneraId
  viajeForm.destino = v.destino

  // Convertir LocalDateTime a string para input date
  viajeForm.fechaViaje = v.fechaViaje.split('T')[0]

  modoEdicionViaje.value = true
}

const actualizarViaje = async () => {
  try {
    // Convertimos la fecha a LocalDateTime completo
    const payload = {
      viajeBomboneraId: viajeForm.viajeBomboneraId,
      destino: viajeForm.destino,
      fechaViaje: viajeForm.fechaViaje + 'T00:00:00'  // hora por defecto
    }

    console.log('Enviando viaje:', JSON.stringify(payload))
    const res = await axios.patch(`http://localhost:8081/viajesBombonera/${payload.viajeBomboneraId}`, payload)

    await cargarViajes()
    modoEdicionViaje.value = false
    Swal.fire({ icon: 'success', title: 'Viaje actualizado', timer: 1500, showConfirmButton: false })
  } catch (e) {
    console.error(e.response?.data || e)
    Swal.fire({ icon: 'error', title: 'Error al actualizar viaje' })
  }
}



const pago = reactive({
  viajePagoId: null,
  viajeId: '',
  monto: null,
  nombre: '',
  apellido: '',
  dni: '',
  metodoPagoId: '',
  cobradorId: ''
})

/* ==========================
   HELPERS
========================== */
const formatFecha = (fechaStr) => {
  try {
    const fecha = new Date(fechaStr)
    return new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(fecha)
  } catch {
    return 'Fecha inválida'
  }
}

const formatoMoneda = (num) => {
  const n = Number(num) || 0
  return new Intl.NumberFormat('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n)
}

const soloNumeros = (campo) => {
  pago[campo] = String(pago[campo]).replace(/\D+/g, '')
}

/* ==========================
   COMPUTEDS / MÉTRICAS
========================== */
const pasajerosPorViaje = computed(() => {
  const result = {}
  for (const v of viajes.value) {
    result[v.viajeBomboneraId] = (pagosPorViaje[v.viajeBomboneraId] || []).length
  }
  return result
})

const totalPorViaje = computed(() => {
  const result = {}
  for (const v of viajes.value) {
    const pagos = pagosPorViaje[v.viajeBomboneraId] || []
    result[v.viajeBomboneraId] = pagos.reduce((sum, p) => sum + (parseFloat(p.monto) || 0), 0)
  }
  return result
})

const pagos = computed(() => {
  return viajeSeleccionado.value ? pagosPorViaje[viajeSeleccionado.value] || [] : []
})

const totalPagos = computed(() => pagos.value.reduce((sum, p) => sum + (parseFloat(p.monto) || 0), 0))

const pagosFiltradosOrdenados = computed(() => {
  let arr = pagos.value
  if (filtroPagos.value) {
    const q = filtroPagos.value.toLowerCase().trim()
    arr = arr.filter((p) =>
      `${p.nombre} ${p.apellido}`.toLowerCase().includes(q) || String(p.dni).includes(q)
    )
  }
  switch (ordenPagos.value) {
    case 'nombre':
      arr = [...arr].sort((a, b) => a.nombre.localeCompare(b.nombre))
      break
    case 'montoAsc':
      arr = [...arr].sort((a, b) => (parseFloat(a.monto) || 0) - (parseFloat(b.monto) || 0))
      break
    case 'montoDesc':
      arr = [...arr].sort((a, b) => (parseFloat(b.monto) || 0) - (parseFloat(a.monto) || 0))
      break
    default:
      arr = [...arr].sort((a, b) => a.apellido.localeCompare(b.apellido))
  }
  return arr
})

const agrupadoPorMetodo = computed(() => {
  return pagos.value.reduce((acc, p) => {
    const key = p.metodoPagoNombre || 'Desconocido'
    acc[key] = (acc[key] || 0) + (parseFloat(p.monto) || 0)
    return acc
  }, {})
})

/* ==========================
   API
========================== */
const cargarMetodosPago = async () => {
  try {
    const res = await axios.get('http://127.0.0.1:8081/metodopago')
    metodosPago.value = res.data
  } catch {
    Swal.fire({ icon: 'error', title: 'Error al cargar métodos de pago' })
  }
}

const cargarCobradores = async () => {
  try {
    const res = await axios.get('http://127.0.0.1:8081/cobradores')
    cobradores.value = res.data
  } catch {
    Swal.fire({ icon: 'error', title: 'Error al cargar cobradores' })
  }
}

const cargarPagos = async (viajeId) => {
  try {
    const res = await axios.get('http://127.0.0.1:8081/viajePagosFull')
    pagosPorViaje[viajeId] = res.data.filter((p) => p.viajeId === viajeId)
  } catch {
    Swal.fire({ icon: 'error', title: 'Error al obtener pagos completos' })
  }
}

const cargarViajes = async () => {
  try {
    isLoadingViajes.value = true
    const res = await axios.get('http://127.0.0.1:8081/viajeBomboneraFiltro', {
      params: {
        filtro: filtroBusqueda.value,
        mes: filtroMes.value,
        pagina: paginaActual.value,
        tamanio: porPagina
      }
    })
    viajes.value = res.data.viajes || []
    totalPaginas.value = Math.ceil((res.data.total || 0) / porPagina)
  } catch {
    Swal.fire({ icon: 'error', title: 'Error al cargar viajes' })
  } finally {
    isLoadingViajes.value = false
  }
}

/* ==========================
   FUNCIONES DE INTERACCIÓN
========================== */
const togglePagos = async (viajeId) => {
  if (viajeSeleccionado.value === viajeId) {
    viajeSeleccionado.value = null
  } else {
    viajeSeleccionado.value = viajeId
    pago.viajeId = viajeId
    await cargarMetodosPago()
    await cargarCobradores()
    cancelarEdicion(false)
    activeTab.value = 'lista'
  }
}

const editarPago = (pagoEditar) => {
  Object.assign(pago, { ...pagoEditar })
  modoEdicion.value = true
  activeTab.value = 'registro'
}

const cancelarEdicion = (volverLista = true) => {
  Object.assign(pago, {
    viajePagoId: null,
    viajeId: viajeSeleccionado.value,
    nombre: '',
    apellido: '',
    dni: '',
    monto: null,
    metodoPagoId: '',
    cobradorId: ''
  })
  modoEdicion.value = false
  if (volverLista) activeTab.value = 'lista'
}

const enviarPago = async () => {
  try {
    enviando.value = true
    if (modoEdicion.value) {
      await axios.patch(`http://localhost:8081/viajepagos/${pago.viajePagoId}`, pago)
      Swal.fire({ icon: 'success', title: 'Pago actualizado', timer: 1500, showConfirmButton: false })
    } else {
      await axios.post('http://127.0.0.1:8081/viajesPagos', pago)
      Swal.fire({ icon: 'success', title: 'Pago registrado', timer: 1500, showConfirmButton: false })
    }
    await cargarPagos(pago.viajeId)
    cancelarEdicion()
    activeTab.value = 'lista'
  } catch {
    Swal.fire({ icon: 'error', title: modoEdicion.value ? 'Error al actualizar pago' : 'Error al registrar pago' })
  } finally {
    enviando.value = false
  }
}

const eliminarPago = async (id) => {
  const confirmado = await Swal.fire({
    title: '¿Estás seguro?',
    text: 'Esta acción eliminará al pasajero del viaje.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar'
  })

  if (confirmado.isConfirmed) {
    try {
      await axios.delete(`http://127.0.0.1:8081/viajePagos/${id}`)
      await cargarPagos(pago.viajeId)
      Swal.fire({ icon: 'success', title: 'Pasajero eliminado', timer: 1500, showConfirmButton: false })
    } catch {
      Swal.fire({ icon: 'error', title: 'Error al eliminar pasajero' })
    }
  }
}

/* ==========================
   FILTROS
========================== */
const limpiarFiltro = () => {
  filtroBusqueda.value = ''
  filtroMes.value = ''
  cargarViajes()
}

/* ==========================
   WATCHERS
========================== */
watch([filtroBusqueda, filtroMes], () => {
  paginaActual.value = 1
  cargarViajes()
})

watch(paginaActual, () => {
  cargarViajes()
})

/* ==========================
   ON MOUNTED
========================== */
onMounted(async () => {
  await Promise.all([cargarViajes(), cargarMetodosPago(), cargarCobradores()])

  // Cargar pagos de todos los viajes para mostrar badges/totales
  for (const v of viajes.value) {
    await cargarPagos(v.viajeBomboneraId)
  }
})
</script>




<style scoped>
.page-header h1 {
  font-size: 2.2rem;
  font-weight: 900;
  color: #003366;
  letter-spacing: 0.02em;
  user-select: none;
}

.page-header h1::after {
  content: "";
  display: block;
  width: 140px;
  height: 4px;
  background: #ffc107;
  margin: 0.25rem auto 0;
  border-radius: 2px;
}

/* Bocastyle buttons */
.btn-boca-primary {
  background-color: #003366;
  color: #ffc107;
  border: none;
  border-radius: 0.5rem;
  padding: 0.65rem 1.25rem;
  font-weight: 700;
  font-size: 1.05rem;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease, color 0.3s ease, transform .05s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 8px rgb(0 51 102 / 0.25);
}

.btn-boca-primary:active {
  transform: translateY(1px);
}

.btn-boca-primary:hover,
.btn-boca-primary:focus {
  background-color: #00509e;
  color: #fff;
  outline: none;
  box-shadow: 0 6px 12px rgb(0 80 158 / 0.5);
}

.btn-outline-boca {
  border: 1px solid #003366;
  color: #003366;
  background: transparent;
  border-radius: .375rem;
  padding: .3rem .75rem;
  font-weight: 600;
  transition: all .2s ease;
}

.btn-outline-boca:hover {
  background-color: #003366;
  color: white;
}

/* Tabs (sin línea amarilla) */
.boca-tabs {
  border-bottom: none;
  margin-bottom: 1rem;
}

.boca-tabs .nav-link {
  color: #003366;
  font-weight: 600;
  font-size: 1rem;
  padding: 0.45rem 1rem;
  border: none;
  border-radius: 0.5rem 0.5rem 0 0;
  margin-right: 0.5rem;
  background-color: #f0f6fc;
  transition: background-color 0.25s ease, color 0.25s ease;
}

.boca-tabs .nav-link.active {
  background-color: #ffc107;
  color: #003366;
  box-shadow: 0 4px 8px rgb(255 193 7 / 0.35);
}

.boca-tabs .nav-link:hover:not(.active) {
  background-color: #d7e6fb;
  color: #003366;
}

/* Sticky tabs inside card */
.sticky-tabs {
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 2;
  padding-top: .25rem;
}

/* Mini cards */
.mini-card {
  background: #f8fafc;
  border: 1px solid #e6ebf2;
  border-radius: .5rem;
  padding: .75rem 1rem;
  height: 100%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, .04);
}

.mini-card .label {
  font-size: .8rem;
  color: #6c757d;
}

.mini-card .value {
  font-size: 1.15rem;
  font-weight: 800;
  color: #003366;
}

/* Tabla pagos */
.scroll-pagos {
  max-height: 360px;
  overflow: auto;
  border: 1px solid #dee2e6;
  border-radius: .5rem;
}

.table> :not(caption)>*>* {
  padding: .6rem .75rem;
}

/* Hover items */
.bg-boca-light {
  background: #f4f8ff;
}

.text-boca-primary {
  color: #003366;
}

/* Animaciones & skeletons */
.fade-enter-active,
.fade-leave-active {
  transition: opacity .18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.skeleton {
  background: linear-gradient(90deg, #f2f5f9 25%, #e9edf3 37%, #f2f5f9 63%);
  background-size: 400% 100%;
  animation: shimmer 1.2s infinite;
  border-radius: .5rem;
}

@keyframes shimmer {
  0% {
    background-position: 100% 0
  }

  100% {
    background-position: -100% 0
  }
}

/* Inputs */
.form-label {
  font-weight: 600;
  color: #003366;
}

.form-control,
.form-select {
  border-radius: 0.5rem;
  box-shadow: 0 1px 6px rgb(0 51 102 / 0.12);
  transition: box-shadow 0.2s ease;
}

.form-control:focus,
.form-select:focus {
  box-shadow: 0 0 8px rgb(0 80 158 / 0.35);
  border-color: #003366;
  outline: none;
}

/* Sticky table header */
.table-responsive .table thead th {
  position: sticky;
  top: 0;
  z-index: 1;
}

/* Search icon */
.bi-search {
  color: #6c757d;
}

/* ==== Viaje cards better UI ==== */
.viaje-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: #f8fbff;
  border: 0;
  border-bottom: 1px solid #e6ebf2;
  cursor: pointer;
  transition: background .2s ease, box-shadow .2s ease;
}

.viaje-header:hover {
  background: #f2f7ff;
  box-shadow: inset 0 -1px 0 #dfe6f3;
}

.viaje-header[aria-expanded="true"] {
  background: #eef5ff;
}

.viaje-header .left {
  display: flex;
  flex-direction: column;
  gap: .35rem;
}

.viaje-header .destino {
  font-size: 1.1rem;
  font-weight: 800;
  color: #0b2d5a;
}

.date-badge {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
  padding: .15rem .5rem;
  font-weight: 700;
  border-radius: .4rem;
  background: #ffc107;
  color: #003366;
  width: max-content;
  box-shadow: 0 2px 6px rgba(255, 193, 7, .25);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: .25rem;
  padding: .25rem .5rem;
  border-radius: 999px;
  background: #fff;
  border: 1px solid #e6ebf2;
  color: #003366;
  font-weight: 600;
  font-size: .9rem;
}

.chip.success {
  color: #136f3a;
  border-color: #d9f2e3;
  background: #f5fff7;
}

.viaje-header .right .ver {
  color: #003366;
  font-weight: 600;
}

.viaje-header .right .bi {
  transition: transform .18s ease;
}

.viaje-header[aria-expanded="true"] .right .bi {
  transform: rotate(180deg);
}
</style>