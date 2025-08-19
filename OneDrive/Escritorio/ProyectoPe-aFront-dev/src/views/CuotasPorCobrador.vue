<template>
  <div class="cobrador-container">
    <h2 class="titulo-reporte">Cuotas Vencidas por Cobrador</h2>

    <!-- Filtros -->
    <div class="form-buscar">
      <select v-model="cobradorSeleccionado" required class="control">
        <option disabled value="">Seleccione un cobrador</option>
        <option
          v-for="cobrador in cobradores"
          :key="cobrador.cobradoresId"
          :value="cobrador.cobradoresId"
        >
          {{ cobrador.nombre }}
        </option>
      </select>

      <input
        type="text"
        v-model="dniFiltro"
        placeholder="DNI de Socio"
        class="control"
        inputmode="numeric"
        @input="dniSoloNumeros"
      />

      <select v-model="anioFiltro" class="control">
        <option value="">Año</option>
        <option v-for="anio in aniosDisponibles" :key="anio" :value="anio">{{ anio }}</option>
      </select>

      <select v-model="mesFiltro" class="control">
        <option value="">Mes</option>
        <option v-for="(nombreMes, index) in meses" :key="index" :value="index + 1">{{ nombreMes }}</option>
      </select>

      <div class="acciones-filtros">
        <button @click="resetFiltros" type="button" class="btn-secundario">
          Limpiar filtros
        </button>
      </div>
    </div>

    <!-- Mensajes -->
    <div
      v-if="Array.isArray(cuotas) && cuotas.length === 0 && cobradorSeleccionado"
      class="alert info"
    >
      No hay cuotas vencidas para este filtro.
    </div>

    <!-- Acciones globales -->
    <div v-if="cuotas.length > 0" class="imprimir-todos-wrapper">
      <button @click="imprimirTodosLosCupones" class="btn-imprimir-todos">
        <i class="fas fa-file-alt"></i> Imprimir todos los cupones
      </button>
    </div>

    <!-- Lista de cupones -->
    <div
      v-for="cuota in cuotas"
      :key="cuota.cuotaId"
      class="cupon-cuota"
      :class="{ pagado: cuota.estado, pendiente: !cuota.estado }"
    >
      <div class="cupon-head">
        <div class="periodo">
          <span class="label">Periodo</span>
          <span class="valor">{{ formatoFecha(cuota.fechaVencimiento).toUpperCase() }}</span>
        </div>
        <span class="badge-estado" :class="cuota.estado ? 'ok' : 'warn'">
          {{ cuota.estado ? 'Pagado' : 'Pendiente' }}
        </span>
      </div>

      <div class="cupon-body">
        <div class="col">
          <p class="line"><strong>Socio:</strong> {{ cuota.nombreSocio }}</p>
          <p class="line"><strong>DNI:</strong> {{ cuota.dni }}</p>
          <p class="line"><strong>Teléfono:</strong> {{ cuota.telefonoSocio }}</p>
        </div>
        <div class="col">
          <p class="line truncate"><strong>Dirección:</strong> {{ cuota.direccionSocio }}</p>
          <div class="importe">
            <span>Monto</span>
            <strong>$ {{ formatoMoneda(cuota.monto) }}</strong>
          </div>
          <p class="line"><strong>Venc.:</strong> {{ fechaCorta(cuota.fechaVencimiento) }}</p>
        </div>
      </div>

      <div class="cupon-foot">
        <div class="meta">
          <span><strong>Cobrador:</strong> {{ cobradorActual?.nombre || '-' }}</span>
          <span><strong>Emisión:</strong> {{ emisionActual }}</span>
          <span><strong>Comprobante:</strong> {{ String(cuota.cuotaId).padStart(8, '0') }}</span>
        </div>

        <div class="acciones">
          <!-- Checkbox selección -->
          <label class="checkbox-pagado">
            <input
              type="checkbox"
              :checked="cuotasSeleccionadas.includes(cuota.cuotaId)"
              :disabled="cuota.estado"
              @change="toggleSeleccionCuota(cuota.cuotaId)"
            />
            <span v-if="!cuota.estado">Seleccionar</span>
            <span v-else>✓ Pagada</span>
          </label>

          <button @click="imprimirCupon(cuota)" class="btn-cupon">
            <i class="fas fa-print"></i> Imprimir cupón
          </button>
        </div>
      </div>

      <!-- Línea de corte indicativa -->
      <div class="cutline"><span>✂ cortar por aquí</span></div>
    </div>

    <!-- Resumen selección (sticky) -->
    <div v-if="cuotas.length > 0" class="seleccion-resumen">
      <div class="stats">
        <span><strong>Seleccionadas:</strong> {{ cantidadSeleccionadas }}</span>
        <span><strong>Total:</strong> $ {{ formatoMoneda(totalSeleccionadas) }}</span>
        <span class="muted" v-if="elegibles.length === 0">No hay pendientes.</span>
      </div>
      <div class="acciones-resumen">
        <button
          class="btn-outline"
          @click="seleccionarTodosPendientes"
          :disabled="elegibles.length === 0"
          title="Seleccionar todas las pendientes visibles"
        >
          Seleccionar todos
        </button>
        <button
          class="btn-outline"
          @click="quitarSeleccion"
          :disabled="cuotasSeleccionadas.length === 0"
        >
          Quitar selección
        </button>
        <button
          class="btn-pagar"
          @click="pagarSeleccionadas"
          :disabled="cuotasSeleccionadas.length === 0"
        >
          <i class="fas fa-check-circle"></i> Marcar pagadas
        </button>
      </div>
    </div>

    <!-- Paginación -->
    <nav class="paginacion" v-if="totalPaginas > 1">
      <button :disabled="paginaActual === 1" @click="paginaActual--">Anterior</button>
      <span>Página {{ paginaActual }} de {{ totalPaginas }}</span>
      <button :disabled="paginaActual === totalPaginas" @click="paginaActual++">Siguiente</button>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'

/* ====== State ====== */
const cobradores = ref([])
const cobradorSeleccionado = ref('')
const cuotas = ref([])

const dniFiltro = ref('')
const anioFiltro = ref('')
const mesFiltro = ref('')

const paginaActual = ref(1)
const pageSize = 20
const totalRegistros = ref(0)
const totalPaginas = ref(1)
const cuotasSeleccionadas = ref([])

const meses = [
  'Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'
]
const aniosDisponibles = []
const añoActual = new Date().getFullYear()
for (let i = 0; i < 3; i++) aniosDisponibles.push(añoActual - i)

const emisionActual = new Date().toLocaleString('es-AR')

/* ====== Cargadores ====== */
const cargarCobradores = async () => {
  try {
    const res = await axios.get('http://localhost:8081/cobradores')
    cobradores.value = res.data
  } catch (err) {
    console.error('Error al cargar cobradores:', err)
    Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudieron cargar los cobradores.' })
  }
}

const cargarCuotas = async () => {
  if (!cobradorSeleccionado.value) {
    cuotas.value = []
    totalRegistros.value = 0
    totalPaginas.value = 1
    return
  }
  try {
    const res = await axios.get('http://localhost:8081/cuotas-vencidas', {
      params: {
        cobradorId: parseInt(cobradorSeleccionado.value),
        dni: dniFiltro.value || undefined,
        mes: mesFiltro.value || undefined,
        anio: anioFiltro.value || undefined,
        page: paginaActual.value,
        pageSize
      }
    })
    cuotas.value = Array.isArray(res.data.data) ? res.data.data : res.data
    totalRegistros.value = res.data.total || cuotas.value.length || 0
    totalPaginas.value = Math.max(1, Math.ceil(totalRegistros.value / pageSize))

    if (cuotas.value.length === 0) {
      Swal.fire({ icon: 'info', title: 'Sin pendientes', text: 'No hay cuotas vencidas para este filtro.' })
    }

    if (paginaActual.value > totalPaginas.value && totalPaginas.value > 0) {
      paginaActual.value = totalPaginas.value
    }
  } catch (err) {
    console.error('Error al cargar cuotas:', err)
    cuotas.value = []
    totalRegistros.value = 0
    totalPaginas.value = 1
    Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudieron cargar las cuotas vencidas.' })
  }
}

/* ====== Helpers ====== */
const formatoMoneda = (num) => {
  const n = Number(num)
  if (Number.isNaN(n)) return '0,00'
  return new Intl.NumberFormat('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n)
}
const formatoFecha = (fechaStr) => {
  const fecha = new Date(fechaStr)
  return fecha.toLocaleDateString('es-AR', { year: 'numeric', month: 'long' })
}
const fechaCorta = (fechaStr) => {
  const f = new Date(fechaStr)
  return isNaN(f) ? '-' : f.toLocaleDateString('es-AR')
}
const dniSoloNumeros = () => {
  dniFiltro.value = (dniFiltro.value || '').replace(/\D+/g, '').slice(0, 10)
}

const cobradorActual = computed(() => {
  const id = parseInt(cobradorSeleccionado.value)
  return cobradores.value.find(c => c.cobradoresId === id) || null
})

/* ====== Selección ====== */
const elegibles = computed(() => cuotas.value.filter(c => !c.estado))
const toggleSeleccionCuota = (cuotaId) => {
  const i = cuotasSeleccionadas.value.indexOf(cuotaId)
  if (i > -1) cuotasSeleccionadas.value.splice(i, 1)
  else cuotasSeleccionadas.value.push(cuotaId)
}
const seleccionarTodosPendientes = () => {
  cuotasSeleccionadas.value = elegibles.value.map(c => c.cuotaId)
}
const quitarSeleccion = () => { cuotasSeleccionadas.value = [] }

const totalSeleccionadas = computed(() =>
  cuotas.value
    .filter(c => cuotasSeleccionadas.value.includes(c.cuotaId))
    .reduce((acc, c) => acc + (Number(c.monto) || 0), 0)
)
const cantidadSeleccionadas = computed(() => cuotasSeleccionadas.value.length)

/* ====== Voucher HTML (doble mitad dentro de un único cupón) ====== */
function voucherHTML(cuota, idx, imprimirSoloUno = false) {
  const logoUrl  = new URL('@/assets/Boca_escudo.png', import.meta.url).href; // ajustá si hace falta
  const montoStr = formatoMoneda(cuota.monto);
  const fechaPer = formatoFecha(cuota.fechaVencimiento);
  const fechaVto = fechaCorta(cuota.fechaVencimiento);
  const compNro  = String(cuota.cuotaId).padStart(8, '0');
  const cobrador = cobradorActual.value?.nombre || '-';

  const header = () => `
    <div class="cabj-head">
      <div class="cabj-left">
        ${logoUrl ? `<img src="${logoUrl}" alt="CABJ" class="cabj-logo" />` : ''}
        <div class="cabj-txt">
          <strong>PEÑA BOQUENSE</strong>
          <small>ANTONIO UBALDO RATTÍN</small>
        </div>
      </div>
    </div>
  `;

  // Mitad izquierda — COPIA COBRADOR (detallada)
  const mitadCobrador = `
    <div class="mitad">
      ${header()}
      <div class="v-body">
        <div class="fila">
          <div><label>Comprobante Nº</label><b>${compNro}</b></div>
          <div><label>Período</label><b>${fechaPer}</b></div>
        </div>

        <div class="fila">
          <div class="span2 truncate"><label>Socio</label><b>${cuota.nombreSocio || '-'}</b></div>
        </div>

        <div class="fila">
          <div><label>DNI</label><b>${cuota.dni || '-'}</b></div>
          <div><label>Teléfono</label><b>${cuota.telefonoSocio || '-'}</b></div>
        </div>

        <div class="fila">
          <div class="span2 truncate"><label>Dirección</label><b>${cuota.direccionSocio || '-'}</b></div>
        </div>

        <div class="fila">
          <div><label>Categoría</label><b>${cuota.categoria || 'Socio Peña'}</b></div>
          <div><label>Vencimiento</label><b>${fechaVto}</b></div>
        </div>

        <div class="total-box">
          <span>TOTAL</span>
          <strong>$ ${montoStr}</strong>
        </div>

        <div class="fila meta">
          <div><label>Cobrador</label><b>${cobrador}</b></div>
        </div>

        <div class="firmas">
          <div>_________________________<br/><small>Firma</small></div>
        </div>
      </div>
    </div>
  `;

  // Mitad derecha — PARTE SOCIO (solo “Se pagó” + monto)
  const mitadSocio = `
    <div class="mitad">
      ${header()}
      <div class="v-body">
        <div class="fila">
          <div><label>Período</label><b>${fechaPer}</b></div>
          <div><label>Vencimiento</label><b>${fechaVto}</b></div>
        </div>
        <div class="fila">
          <div class="span2"><label>Comprobante Nº</label><b>${compNro}</b></div>
        </div>
        <div class="pago-box">
          <div class="pago-titulo">SE PAGÓ</div>
          <div class="pago-monto">$ ${montoStr}</div>
        </div>
      </div>
    </div>
  `;

  // ⚠️ IMPORTANTE: sin <div class="page-break"> para que entren 3 por hoja
  return `
    <section class="ticket ${imprimirSoloUno ? 'single' : ''}">
      ${mitadCobrador}
      <div class="corte-vertical" aria-hidden="true"><span>✂</span></div>
      ${mitadSocio}
    </section>
  `;
}



const imprimirCupon = (cuota) => {
  const w = window.open('', '_blank')
  if (!w) {
    Swal.fire({ icon: 'warning', title: 'Pop-up bloqueado', text: 'Habilitá los pop-ups para imprimir.' })
    return
  }
  const html = buildPrintHTML([voucherHTML(cuota, 0, true)])
  w.document.open(); w.document.write(html); w.document.close()
}

const imprimirTodosLosCupones = () => {
  if (cuotas.value.length === 0) {
    return Swal.fire({ icon: 'info', title: 'Sin cupones', text: 'No hay cuotas en el listado actual.' })
  }
  const w = window.open('', '_blank')
  if (!w) {
    Swal.fire({ icon: 'warning', title: 'Pop-up bloqueado', text: 'Habilitá los pop-ups para imprimir.' })
    return
  }
  const bloques = cuotas.value.map((c, i) => voucherHTML(c, i))
  const html = buildPrintHTML(bloques)
  w.document.open(); w.document.write(html); w.document.close()
}

function buildPrintHTML(sections) {
  return `
  <!doctype html>
  <html lang="es">
  <head>
    <meta charset="utf-8" />
    <title>Cupones de Cuotas</title>
    <style>
      /* Hoja A4 vertical */
      @page { size: A4; margin: 8mm; }
      * { box-sizing: border-box; }
      body {
        font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
        color: #0b3c5d;
        -webkit-print-color-adjust: exact; print-color-adjust: exact;
        margin: 0;
      }

      /* === Cupón apaisado (entra 3 por hoja) ===
         - Alto aprox 85mm + márgenes (5mm arriba/abajo)
         - Ancho 185mm centrado                                         */
      .ticket {
        width: 185mm;          /* ancho grande (apaisado) */
        min-height: 85mm;      /* alto menor para que entren 3 */
        margin: 5mm auto;      /* centrado + separaciones */
        border: 2px solid #003366;
        border-radius: 12px;
        background: #fff;
        display: grid;
        grid-template-columns: 1fr 12px 1fr; /* mitad izq | corte | mitad der */
        overflow: hidden;
        break-inside: avoid;   /* que no se corte el cupón a mitad de página */
      }

      /* Mitades */
      .mitad { display: flex; flex-direction: column; min-height: 85mm; }

      /* Encabezado con colores Boca + escudo */
      .cabj-head {
        display:flex; justify-content:space-between; align-items:center;
        background:#003366; color:#fff; padding:8px 10px; border-bottom:2px solid #ffc107;
      }
      .cabj-left { display:flex; align-items:center; gap:8px; }
      .cabj-logo { width:26px; height:26px; object-fit:contain; }
      .cabj-txt strong { letter-spacing:.02em; }
      .cabj-txt small { display:block; color:#ffc107; font-weight:800; line-height:1.1; }

      /* Cuerpo */
      .v-body { padding: 8px 10px 10px; flex:1; display:flex; flex-direction:column; gap:6px; }
      .fila { display:grid; grid-template-columns: 1fr 1fr; gap:6px 10px; align-items:end; }
      .fila .span2 { grid-column: span 2; }
      label { display:block; font-size:11px; color:#62708a; }
      b { font-size:13px; color:#0b3c5d; }
      .truncate { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }

      /* Total y pago (resaltado) */
      .total-box, .pago-box {
        border:1px solid #e6eef8; background:#f7fbff; border-radius:10px; padding:8px 10px;
        display:flex; align-items:center; justify-content:space-between;
      }
      .total-box span { color:#607089; font-weight:700; }
      .total-box strong, .pago-monto { font-size:18px; color:#003366; letter-spacing:.02em; }
      .pago-titulo { color:#003366; font-weight:900; }

      /* Firmas centradas */
      .firmas {
        display:grid; grid-template-columns:1fr 1fr; gap:10px;
        justify-items: center; align-items: center; text-align:center; margin-top:auto;
      }
      .firmas small { color:#607089; }

      /* Línea de corte vertical */
      .corte-vertical {
        border-left:2px dotted #94a3b8; border-right:2px dotted #94a3b8;
        position:relative;
        background: repeating-linear-gradient(180deg, transparent 0 6mm, rgba(0,0,0,0.02) 6mm 6.2mm);
      }
      .corte-vertical span {
        position:absolute; top:6px; left:50%; transform:translateX(-50%);
        font-size:12px; color:#64748b;
      }
    </style>
  </head>
  <body>
    ${sections.join('')}
    <script>
      window.onload = function(){ window.print(); window.close(); };
    <\/script>
  </body>
  </html>`;
}



/* ====== Pago de seleccionadas (intuitivo + alertas) ====== */
const pagarSeleccionadas = async () => {
  if (cuotasSeleccionadas.value.length === 0) {
    return Swal.fire({ icon: 'warning', title: 'Atención', text: 'Seleccioná al menos una cuota para marcar como pagada.' })
  }

  const total = formatoMoneda(totalSeleccionadas.value)
  const { isConfirmed } = await Swal.fire({
    icon: 'question',
    title: 'Confirmar pago',
    html: `Vas a marcar como pagadas <b>${cuotasSeleccionadas.value.length}</b> cuota(s)
           por <b>$ ${total}</b>. ¿Continuar?`,
    showCancelButton: true,
    confirmButtonText: 'Sí, marcar pagadas',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#003366'
  })
  if (!isConfirmed) return

  try {
    Swal.fire({
      title: 'Procesando...',
      text: 'Actualizando pagos',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    })

    // marcar una por una (si tu backend soporta batch, acá podrías enviar un arreglo)
    await Promise.all(
      cuotasSeleccionadas.value.map(async (id) => {
        await axios.put(`http://localhost:8081/cuotas/pagar/${id}`)
        const c = cuotas.value.find(x => x.cuotaId === id)
        if (c) c.estado = true
      })
    )

    Swal.close()
    await Swal.fire({ icon: 'success', title: 'Pago realizado', text: 'Cuotas marcadas como pagadas.' })
    cuotasSeleccionadas.value = []
    await cargarCuotas()
  } catch (err) {
    console.error('Error al pagar cuotas:', err)
    Swal.close()
    Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un error al pagar algunas cuotas.' })
  }
}

/* ====== Filtros ====== */
const resetFiltros = () => {
  dniFiltro.value = ''
  anioFiltro.value = ''
  mesFiltro.value = ''
  paginaActual.value = 1
  cobradorSeleccionado.value = ''
  cuotasSeleccionadas.value = []
}

/* ====== Lifecycle & watchers ====== */
onMounted(() => { cargarCobradores() })

watch([cobradorSeleccionado, dniFiltro, anioFiltro, mesFiltro], () => {
  paginaActual.value = 1
  cargarCuotas()
  cuotasSeleccionadas.value = []
}, { immediate: true })

watch(paginaActual, () => {
  cargarCuotas()
  cuotasSeleccionadas.value = []
})
</script>

<style scoped>
.cobrador-container {
  max-width: 1050px;
  margin: 2rem auto;
  background: #ffffff;
  padding: 1.25rem 1.25rem 1.5rem;
  border-radius: 16px;
  box-shadow: 0 10px 28px rgba(0,0,0,.06);
  color: #0b3c5d;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Título */
.titulo-reporte {
  font-weight: 900;
  font-size: 2rem;
  color: #003366;
  text-align: center;
  margin-bottom: 1rem;
  position: relative;
  user-select: none;
}
.titulo-reporte::after {
  content: "";
  display: block;
  width: 140px;
  height: 4px;
  background: #ffc107;
  margin: 0.25rem auto 0;
  border-radius: 2px;
}

/* Filtros */
.form-buscar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 1.1rem;
  align-items: center;
}
.control {
  padding: 10px 14px;
  font-size: 1rem;
  border-radius: 10px;
  border: 1.5px solid #cbd5e1;
  width: 100%;
  transition: all .25s ease;
  background: #fff;
}
.control:focus {
  border-color: #00509e;
  outline: none;
  box-shadow: 0 0 0 3px rgba(0, 80, 158, .2);
}
.acciones-filtros {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-secundario,
.btn-imprimir-todos,
.btn-primario {
  background-color: #003366;
  color: #ffc107;
  border: none;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 800;
  font-size: .98rem;
  cursor: pointer;
  transition: .25s ease;
  margin-bottom: 10px;
}
.btn-secundario:hover,
.btn-imprimir-todos:hover,
.btn-primario:hover { background-color: #00509e; color: white; }

.btn-primario.alt { background: #173a72; }
.btn-primario.alt:hover { background: #1e4a9a; }

/* Mensajes */
.alert.info {
  background: #eef6ff;
  border: 1px solid #dbeafe;
  color: #0b3c5d;
  border-radius: 10px;
  padding: .75rem 1rem;
  text-align: center;
}

/* Cupón (lista) */
.cupon-cuota {
  border: 2px dashed #003366;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 1rem;
  background-color: #ffffff;
  box-shadow: 0 6px 16px rgba(0,0,0,.03);
}
.cupon-head {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  padding-bottom: 6px; border-bottom: 1px solid #e6ebf4;
}
.periodo .label { font-size: .8rem; color: #607089; display: block; }
.periodo .valor { font-weight: 900; color: #0b3c5d; }
.badge-estado {
  border-radius: 999px; font-size: .78rem; padding: .15rem .55rem; font-weight: 800;
  border: 1px solid transparent; white-space: nowrap;
}
.badge-estado.ok { background: #e7f6ec; color: #136f3a; border-color: #cfeedd; }
.badge-estado.warn { background: #fdecec; color: #a11328; border-color: #f7d0d6; }

.cupon-body {
  display: grid; grid-template-columns: 1fr 1fr; gap: 8px 14px; padding-top: 8px;
}
.line { margin: 2px 0; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.importe {
  background: #f7fbff; border: 1px solid #e6eef8; padding: 8px 10px; border-radius: 10px;
  display: flex; align-items: center; justify-content: space-between; gap: 8px; margin: 4px 0;
  font-weight: 800;
}
.importe span { color: #607089; font-weight: 700; }

.cupon-foot {
  display: flex; flex-wrap: wrap; gap: 10px; justify-content: space-between; align-items: center;
  margin-top: 8px; padding-top: 8px; border-top: 1px dashed #cfd8ea;
}
.meta { display: flex; gap: 12px; flex-wrap: wrap; color: #334155; font-size: .92rem; }

.checkbox-pagado {
  display: inline-flex; align-items: center; gap: 8px;
  font-size: .95rem; font-weight: 700; color: #003366;
}
.checkbox-pagado input[type="checkbox"] {
  width: 18px; height: 18px; accent-color: #2e7d32; cursor: pointer;
}

.btn-cupon {
  background-color: #003366; color: #ffc107; padding: 8px 14px;
  border: none; border-radius: 10px; font-weight: 800; cursor: pointer;
}
.btn-cupon:hover { background-color: #00509e; color: white; }

.cutline {
  border-top: 2px dotted #94a3b8; margin: 12px 0 0; position: relative; height: 0;
}
.cutline span {
  position: absolute; top: -12px; left: 8px; font-size: .82rem; color: #64748b; background: #fff; padding: 0 4px;
}

/* Resumen selección (sticky) */
.seleccion-resumen {
  position: sticky; bottom: 0; left: 0; right: 0;
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  background: rgba(194, 193, 193, 0.95); backdrop-filter: blur(6px);
  border: 1px solid #e5e7eb; border-radius: 12px; padding: .7rem .9rem; margin-top: 1rem;
}
.seleccion-resumen .stats { display: flex; gap: 14px; flex-wrap: wrap; align-items: center; }
.seleccion-resumen .stats .muted { color: #6b7280; }
.acciones-resumen { display: flex; gap: 8px; flex-wrap: wrap; }
.btn-outline {
  background: #fff; border: 1px solid #e5e7eb; color: #111827; padding: .5rem .85rem;
  border-radius: 10px; font-weight: 700; cursor: pointer;
}
.btn-outline:hover { background: #f3f4f6; }

.btn-pagar {
  background-color: #2e7d32; color: white; padding: 10px 16px;
  border: none; border-radius: 10px; cursor: pointer; font-weight: 800;
}
.btn-pagar:hover { background-color: #1b5e20; }

/* Paginación */
.paginacion {
  display: flex; gap: 12px; justify-content: center; align-items: center; margin-top: .9rem;
}
.paginacion button {
  background: #fff; border: 1px solid #dbe6ff; padding: 6px 12px; border-radius: 10px; cursor: pointer; font-weight: 700;
}
.paginacion button:disabled { opacity: .5; cursor: not-allowed; }
.paginacion span { color: #0b3c5d; font-weight: 700; }

/* Responsive */
@media (max-width: 680px) {
  .cupon-body { grid-template-columns: 1fr; }
  .control { width: 100%; }
  .acciones-filtros { justify-content: stretch; }
}

@media (max-width: 560px) {
  .seleccion-resumen {
    flex-direction: column;       /* apila stats + acciones */
    align-items: stretch;          /* estira al ancho */
    gap: 10px;
  }

  .seleccion-resumen .stats {
    display: grid;
    grid-template-columns: 1fr;    /* cada item en su fila */
    gap: 6px;
  }

  .acciones-resumen {
    width: 100%;
    display: grid;                 /* botones en columna */
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .acciones-resumen .btn-outline,
  .acciones-resumen .btn-pagar {
    width: 100%;                   /* full width */
  }
}
/* Separación entre checkbox y botón en desktop */
.cupon-foot .acciones {
  display: flex;
  gap: 14px;
  align-items: center;
}


</style>
