<template>
  <div class="container formulario-container shadow p-4 rounded mt-5 mb-5">
    <header class="page-header mb-4">
      <h2>Movimientos</h2>
    </header>

    <!-- Tabs -->
    <ul class="nav nav-tabs nav-boca mb-4" role="tablist">
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          :class="{ active: tabActivo === 'registro' }"
          @click="tabActivo = 'registro'"
          type="button"
          role="tab"
          :aria-selected="tabActivo === 'registro'"
          :tabindex="tabActivo === 'registro' ? 0 : -1"
          id="tab-registro"
          aria-controls="panel-registro"
        >
          Registro
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          :class="{ active: tabActivo === 'historial' }"
          @click="tabActivo = 'historial'"
          type="button"
          role="tab"
          :aria-selected="tabActivo === 'historial'"
          :tabindex="tabActivo === 'historial' ? 0 : -1"
          id="tab-historial"
          aria-controls="panel-historial"
        >
          Historial
        </button>
      </li>
    </ul>

    <!-- Panel Registro -->
    <section
      v-if="tabActivo === 'registro'"
      role="tabpanel"
      aria-labelledby="tab-registro"
      id="panel-registro"
    >
      <form
        @submit.prevent="modoEdicion ? actualizarMovimiento() : crearMovimiento()"
        class="row g-3 needs-validation"
        novalidate
        autocomplete="off"
      >
        <div class="col-md-6">
          <label for="monto" class="form-label fw-semibold">
            Monto <span class="text-danger">*</span>
          </label>
          <div class="input-group">
            <span class="input-group-text">$</span>
            <input
              id="monto"
              v-model.number="formularioMovimiento.monto"
              type="number"
              step="0.01"
              min="0"
              class="form-control form-control-boca"
              placeholder="1000.00"
              required
              :class="{ 'is-invalid': errores.monto }"
              aria-describedby="error-monto"
            />
          </div>
          <div id="error-monto" class="invalid-feedback">
            El monto es obligatorio y debe ser positivo.
          </div>
        </div>

        <div class="col-md-6">
          <label for="descripcion" class="form-label fw-semibold">
            Descripción <span class="text-danger">*</span>
          </label>
          <input
            id="descripcion"
            v-model="formularioMovimiento.descripcion"
            type="text"
            class="form-control form-control-boca"
            placeholder="Descripción breve"
            required
            :class="{ 'is-invalid': errores.descripcion }"
            aria-describedby="error-descripcion"
          />
          <div id="error-descripcion" class="invalid-feedback">
            La descripción es obligatoria.
          </div>
        </div>

        <div class="col-md-6">
          <label for="tipo" class="form-label fw-semibold">
            Tipo de Movimiento <span class="text-danger">*</span>
          </label>
          <select
            id="tipo"
            v-model="formularioMovimiento.tipo"
            class="form-select form-select-boca"
            required
            :class="{ 'is-invalid': errores.tipo }"
            aria-describedby="error-tipo"
          >
            <option disabled value="">Seleccione</option>
            <option value="ingreso">Ingreso</option>
            <option value="egreso">Egreso</option>
          </select>
          <div id="error-tipo" class="invalid-feedback">
            Seleccione un tipo de movimiento.
          </div>
        </div>

        <div class="col-md-6">
          <label for="fecha" class="form-label fw-semibold">
            Fecha <span class="text-danger">*</span>
          </label>
          <input
            id="fecha"
            v-model="formularioMovimiento.fecha"
            type="datetime-local"
            class="form-control form-control-boca"
            required
            :class="{ 'is-invalid': errores.fecha }"
            aria-describedby="error-fecha"
          />
          <div id="error-fecha" class="invalid-feedback">
            La fecha es obligatoria.
          </div>
        </div>

        <div class="col-md-6">
          <label for="metodoPagoId" class="form-label fw-semibold">
            Método de Pago <span class="text-danger">*</span>
          </label>
          <select
            id="metodoPagoId"
            v-model.number="formularioMovimiento.metodoPagoId"
            class="form-select form-select-boca"
            required
            :class="{ 'is-invalid': errores.metodoPagoId }"
            aria-describedby="error-metodoPagoId"
          >
            <option disabled value="">Seleccione un método</option>
            <option
              v-for="metodo in metodosPago"
              :key="metodo.metodoPagoId"
              :value="metodo.metodoPagoId"
            >
              {{ metodo.nombre }}
            </option>
          </select>
          <div id="error-metodoPagoId" class="invalid-feedback">
            Seleccione un método de pago.
          </div>
        </div>

        <div class="col-12 d-flex justify-content-center gap-3 mt-4">
          <button
            type="submit"
            class="btn btn-boca-primary btn-lg px-4"
            :disabled="cargando"
            aria-live="polite"
          >
            <span
              v-if="cargando"
              class="spinner-border spinner-border-sm me-2"
              role="status"
              aria-hidden="true"
            ></span>
            {{ modoEdicion ? "Guardar Cambios" : "Agregar Movimiento" }}
          </button>

          <button
            v-if="modoEdicion"
            type="button"
            class="btn btn-boca-secondary btn-lg px-4"
            @click="cancelarEdicion"
            :disabled="cargando"
          >
            Cancelar
          </button>
        </div>
      </form>
    </section>

    <!-- Panel Historial -->
    <section
      v-if="tabActivo === 'historial'"
      role="tabpanel"
      aria-labelledby="tab-historial"
      id="panel-historial"
    >
      <!-- Toolbar historial -->
      <div class="historial-toolbar mb-3 d-flex align-items-center gap-3 flex-wrap">
        <div class="d-flex align-items-center gap-2">
          <label for="filtroSeleccionado" class="form-label fw-semibold mb-0">Rango:</label>
          <select
            id="filtroSeleccionado"
            v-model="filtroSeleccionado"
            @change="cargarMovimientos"
            class="form-select w-auto form-select-boca"
          >
            <option value="semanal">Últimos 7 días</option>
            <option value="mensual">Por mes</option>
          </select>
          <input
            v-if="filtroSeleccionado === 'mensual'"
            type="month"
            v-model="mesSeleccionado"
            @change="cargarMovimientos"
            class="form-control w-auto form-control-boca"
            :max="maxMes"
          />
        </div>

        <div class="ms-auto d-flex align-items-center gap-2">
          <input
            v-model="busqueda"
            @input="aplicarBusqueda"
            class="form-control form-control-boca"
            placeholder="Buscar descripción…"
            style="min-width: 240px"
          />
          <select v-model="ordenTabla" class="form-select form-select-boca w-auto">
            <option value="fechaDesc">Fecha (↓)</option>
            <option value="fechaAsc">Fecha (↑)</option>
            <option value="montoDesc">Monto (↓)</option>
            <option value="montoAsc">Monto (↑)</option>
          </select>
        </div>
      </div>

      <!-- Tabla -->
      <div class="table-wrapper rounded shadow-sm border">
        <table class="table table-hover align-middle mb-0 movimientos-table">
          <thead class="table-light sticky-top">
            <tr>
              <th class="text-end">Monto</th>
              <th>Descripción</th>
              <th class="text-center">Tipo</th>
              <th class="text-nowrap">Fecha</th>
              <th class="text-nowrap">Método</th>
              <th class="text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="m in movimientosOrdenados"
              :key="m.movimientoId"
              class="fila-movimiento"
            >
              <td class="text-end monto">
                <span :class="m.tipo === 'ingreso' ? 'text-success' : 'text-danger'">
                  <i :class="m.tipo === 'ingreso' ? 'bi bi-caret-up-fill' : 'bi bi-caret-down-fill'" aria-hidden="true"></i>
                  ${{ formatoMoneda(m.monto) }}
                </span>
              </td>
              <td class="descripcion" :title="m.descripcion">{{ m.descripcion }}</td>
              <td class="text-center">
                <span :class="['badge', m.tipo === 'ingreso' ? 'bg-success-soft' : 'bg-danger-soft']">
                  {{ m.tipo.charAt(0).toUpperCase() + m.tipo.slice(1) }}
                </span>
              </td>
              <td class="text-nowrap">{{ formatearFecha(m.fecha) }}</td>
              <td class="text-nowrap">
                <span class="badge bg-method">{{ nombreMetodoPago(m.metodoPagoId) }}</span>
              </td>
              <td class="text-center acciones">
                <button
                  @click="abrirFormularioEdicion(m); tabActivo = 'registro'"
                  class="icon-btn icon-warning"
                  aria-label="Editar movimiento"
                  title="Editar"
                >
                  <i class="bi bi-pencil-fill"></i>
                </button>
                <button
                  @click="eliminarMovimiento(m.movimientoId)"
                  class="icon-btn icon-danger"
                  aria-label="Eliminar movimiento"
                  title="Eliminar"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
            <tr v-if="movimientosOrdenados.length === 0">
              <td colspan="6" class="text-center text-muted py-4">No hay movimientos para mostrar.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Resumen -->
      <div class="resumen alert alert-light border mt-4 rounded">
        <div class="row g-3 text-center text-md-start">
          <div class="col-12 col-md-4">
            <div class="stat-card">
              <span class="label">Balance Total</span>
              <span class="value">${{ formatoMoneda(balanceTotal) }}</span>
            </div>
          </div>
          <div class="col-12 col-md-8 d-flex flex-wrap gap-2 align-items-center">
            <span class="label me-2">Por método de pago:</span>
            <span class="badge bg-light text-dark border px-3 py-2 rounded">Efectivo: ${{ formatoMoneda(totalEfectivo) }}</span>
            <span class="badge bg-light text-dark border px-3 py-2 rounded">Transferencia: ${{ formatoMoneda(totalTransferencia) }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import axios from 'axios'
import Swal from 'sweetalert2'

export default {
  name: 'Movimientos',
  data() {
    return {
      tabActivo: 'registro',
      movimientosFiltrados: [],
      balanceTotal: 0,
      totalEfectivo: 0,
      totalTransferencia: 0,
      modoEdicion: false,
      cargando: false,
      filtroSeleccionado: 'mensual',
      mesSeleccionado: this.obtenerMesActual(),
      maxMes: this.obtenerMesActual(),
      metodosPago: [],
      formularioMovimiento: {
        movimientoId: null,
        monto: null,
        descripcion: '',
        tipo: '',
        fecha: '',
        metodoPagoId: null,
      },
      errores: {},
      // nuevos (solo UI/UX)
      busqueda: '',
      ordenTabla: 'fechaDesc',
    }
  },
  mounted() {
    this.cargarMetodosPago()
    this.cargarMovimientos()
  },
  watch: {
    tabActivo(nuevo) {
      if (nuevo === 'historial') this.cargarMovimientos()
    },
  },
  computed: {
    movimientosBuscados() {
      const q = this.busqueda.trim().toLowerCase()
      if (!q) return this.movimientosFiltrados
      return this.movimientosFiltrados.filter(m =>
        (m?.descripcion || '').toLowerCase().includes(q)
      )
    },
    movimientosOrdenados() {
      const arr = [...this.movimientosBuscados]
      switch (this.ordenTabla) {
        case 'fechaAsc':
          return arr.sort((a,b) => new Date(a.fecha) - new Date(b.fecha))
        case 'montoDesc':
          return arr.sort((a,b) => (parseFloat(b.monto)||0) - (parseFloat(a.monto)||0))
        case 'montoAsc':
          return arr.sort((a,b) => (parseFloat(a.monto)||0) - (parseFloat(b.monto)||0))
        default:
          return arr.sort((a,b) => new Date(b.fecha) - new Date(a.fecha))
      }
    }
  },
  methods: {
    aplicarBusqueda() {
      // solo para disparar reactividad y mantener página en 1 si hubiera paginado
    },
    formatoMoneda(num) {
      if (isNaN(num)) return '0,00'
      return new Intl.NumberFormat('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num)
    },
    obtenerMesActual() {
      const hoy = new Date()
      return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}`
    },
    async cargarMetodosPago() {
      try {
        const res = await axios.get('http://localhost:8081/metodopago')
        this.metodosPago = res.data
      } catch (error) {
        console.error('Error cargando métodos de pago:', error)
      }
    },
    validarFormulario() {
      this.errores = {}
      if (!this.formularioMovimiento.monto || this.formularioMovimiento.monto <= 0) this.errores.monto = true
      if (!this.formularioMovimiento.descripcion) this.errores.descripcion = true
      if (!this.formularioMovimiento.tipo) this.errores.tipo = true
      if (!this.formularioMovimiento.fecha) this.errores.fecha = true
      if (!this.formularioMovimiento.metodoPagoId) this.errores.metodoPagoId = true
      return Object.keys(this.errores).length === 0
    },
    async crearMovimiento() {
      if (!this.validarFormulario()) { window.scrollTo({ top: 0, behavior: 'smooth' }); return }
      this.cargando = true
      try {
        const data = { ...this.formularioMovimiento, monto: parseFloat(this.formularioMovimiento.monto) }
        await axios.post('http://localhost:8081/movimientos', data)
        this.resetFormulario()
        this.refrescarListaConFiltro()
        this.tabActivo = 'historial'
      } catch (error) {
        Swal.fire('Error', `Error al crear movimiento: ${error.response?.data?.error || error.message}`, 'error')
      } finally { this.cargando = false }
    },
    async actualizarMovimiento() {
      if (!this.validarFormulario()) { window.scrollTo({ top: 0, behavior: 'smooth' }); return }
      this.cargando = true
      try {
        const id = this.formularioMovimiento.movimientoId
        const data = { ...this.formularioMovimiento, monto: parseFloat(this.formularioMovimiento.monto) }
        await axios.patch(`http://localhost:8081/movimientos/${id}`, data)
        this.resetFormulario()
        this.modoEdicion = false
        this.refrescarListaConFiltro()
        this.tabActivo = 'historial'
      } catch (error) {
        Swal.fire('Error', `Error al actualizar movimiento: ${error.response?.data?.error || error.message}`, 'error')
      } finally { this.cargando = false }
    },
    abrirFormularioEdicion(movimiento) {
      this.formularioMovimiento = { ...movimiento }
      if (this.formularioMovimiento.fecha && !this.formularioMovimiento.fecha.includes('T')) {
        this.formularioMovimiento.fecha = this.formularioMovimiento.fecha.replace(' ', 'T')
      }
      this.modoEdicion = true
      this.tabActivo = 'registro'
      this.errores = {}
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    cancelarEdicion() {
      if (this.cargando) return
      this.resetFormulario()
      this.modoEdicion = false
      this.errores = {}
    },
    async cargarMovimientos() {
      try {
        if (this.filtroSeleccionado === 'semanal') {
          const hasta = new Date()
          const desde = new Date(); desde.setDate(hasta.getDate() - 7)
          const desdeStr = desde.toISOString().split('T')[0]
          const hastaStr = hasta.toISOString().split('T')[0]
          const res = await axios.get('http://localhost:8081/balance/semanal', { params: { desde: desdeStr, hasta: hastaStr } })
          this.movimientosFiltrados = res.data.movimientos || []
          this.balanceTotal = Number(res.data.balanceTotal) || 0
          this.totalEfectivo = Number(res.data.totalEfectivo) || 0
          this.totalTransferencia = Number(res.data.totalTransferencia) || 0
        } else if (this.filtroSeleccionado === 'mensual') {
          if (!this.mesSeleccionado) { alert('Por favor selecciona un mes válido'); return }
          const res = await axios.get('http://localhost:8081/balance/mensual', { params: { mes: this.mesSeleccionado } })
          this.movimientosFiltrados = res.data.movimientos || []
          this.balanceTotal = Number(res.data.balanceTotal) || 0
          this.totalEfectivo = Number(res.data.totalEfectivo) || 0
          this.totalTransferencia = Number(res.data.totalTransferencia) || 0
        } else {
          const resMovs = await axios.get('http://localhost:8081/movimientos')
          this.movimientosFiltrados = resMovs.data
          const resBal = await axios.get('http://localhost:8081/balance')
          this.balanceTotal = Number(resBal.data.balanceTotal) || 0
          this.totalEfectivo = 0
          this.totalTransferencia = 0
        }
      } catch (error) {
        console.error('Error cargando movimientos o balances:', error)
      }
    },
    refrescarListaConFiltro() { this.cargarMovimientos() },
    async eliminarMovimiento(id) {
      const result = await Swal.fire({
        title: '¿Está seguro?',
        text: 'No podrá revertir esta acción.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#00529F',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar',
      })
      if (result.isConfirmed) {
        try {
          await axios.delete(`http://localhost:8081/movimientos/${id}`)
          Swal.fire('Eliminado', 'El movimiento fue eliminado.', 'success')
          this.cargarMovimientos()
        } catch (error) {
          Swal.fire('Error', `Error al eliminar movimiento: ${error.response?.data?.error || error.message}`, 'error')
        }
      }
    },
    formatearFecha(fechaISO) {
      if (!fechaISO) return '-'
      const fecha = new Date(fechaISO)
      return fecha.toLocaleString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    },
    nombreMetodoPago(id) {
      const metodo = this.metodosPago.find(m => m.metodoPagoId === id)
      return metodo ? metodo.nombre : '-'
    },
    resetFormulario() {
      this.formularioMovimiento = { movimientoId: null, monto: null, descripcion: '', tipo: '', fecha: '', metodoPagoId: null }
      this.errores = {}
    },
  },
}
</script>

<style scoped>
.page-header h2 { font-size: 2.4rem; font-weight: 900; color: #003366; text-align: center; margin-bottom: .2rem; letter-spacing: .02em; user-select: none; position: relative; }
.page-header h2::after { content: ""; display: block; width: 140px; height: 4px; background: #ffc107; margin: .25rem auto 0; border-radius: 2px; }

.formulario-container { max-width: 1100px; margin: 40px auto; background-color: #fff; color: #0B3C5D; font-family: "Segoe UI", Roboto, "Helvetica" }

.nav-boca { border-bottom: 3px solid #00529F; display: flex; gap: 12px; }
.nav-link { border-radius: 8px 8px 0 0; font-weight: 700; color: #00529F; padding: .55rem 1.5rem; border: 2.5px solid transparent; cursor: pointer; user-select: none; font-size: 1.1rem; transition: background-color .25s ease, border-color .25s ease; }
.nav-link.active { background-color: #003366; border-color: #00529F #00529F transparent #00529F; color: #ffc107; font-weight: 800; }
.nav-link:hover:not(.active) { background-color: #e1eaff; }

.form-control-boca, .form-select-boca { border-radius: 8px; border: 1.5px solid #00529F; padding: .6rem 1rem; font-size: 1rem; transition: border-color .3s ease, box-shadow .3s ease; color: #00529F; }
.form-control-boca:focus, .form-select-boca:focus { border-color: #ffc107; box-shadow: 0 0 8px rgba(255, 215, 0, 0.5); outline: none; }
.invalid-feedback { font-size: .9rem; color: #d9534f; font-weight: 600; }

.btn-boca-primary { background-color: #003366; border: none; color: #ffc107; font-weight: 700; padding: .65rem 2.2rem; border-radius: 10px; font-size: 1.05rem; transition: background-color .3s ease; }
.btn-boca-primary:hover:not(:disabled) { background-color: #013C8A; color: #fff; }
.btn-boca-secondary { background-color: #ffc107; border: none; color: #003366; font-weight: 700; padding: .65rem 2.2rem; border-radius: 10px; font-size: 1.05rem; transition: background-color .3s ease; }
.btn-boca-secondary:hover:not(:disabled) { background-color: #ffc107; color: #013C8A; }

/* Toolbar */
.historial-toolbar .form-control-boca, .historial-toolbar .form-select-boca { min-height: 40px; }

/* Tabla mejorada */
.table-wrapper { max-height: 60vh; overflow: auto; }
.movimientos-table thead th { position: sticky; top: 0; z-index: 1; }
.movimientos-table tbody tr:nth-child(odd) { background: #fafcff; }
.movimientos-table tbody tr:hover { background: #f2f7ff; }
.movimientos-table td, .movimientos-table th { vertical-align: middle; }
.movimientos-table .monto { font-weight: 700; }
.movimientos-table .descripcion { max-width: 420px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Badges suaves */
.bg-success-soft { background: #e9f8ef; color: #136f3a; border: 1px solid #cfeedd; padding: .4rem .6rem; font-weight: 700; }
.bg-danger-soft { background: #fdecee; color: #a11328; border: 1px solid #f7d0d6; padding: .4rem .6rem; font-weight: 700; }
.bg-method { background: #eef4ff; color: #173a72; border: 1px solid #dbe6ff; }

/* Icon buttons */
.icon-btn { background: transparent; border: none; padding: 6px; cursor: pointer; color: #003366; font-size: 1.1rem; transition: color .2s ease, transform .05s ease; display: inline-flex; align-items: center; justify-content: center; border-radius: .5rem; }
.icon-btn:hover { color: #00509e; }
.icon-btn:active { transform: translateY(1px); }
.icon-warning:hover { color: #ffc107; }
.icon-danger:hover { color: #dc3545; }

/* Resumen */
.stat-card { background: #f8fafc; border: 1px solid #e6ebf2; border-radius: .5rem; padding: .75rem 1rem; display: inline-flex; flex-direction: column; gap: .25rem; }
.stat-card .label { font-size: .82rem; color: #6c757d; }
.stat-card .value { font-size: 1.25rem; font-weight: 800; color: #003366; }

@media (max-width: 576px) {
  /* En móviles ocultar columna método de pago para ahorrar espacio */
  .movimientos-table th:nth-child(5), .movimientos-table td:nth-child(5) { display: none; }
  .movimientos-table .descripcion { max-width: 220px; }
}
</style>
