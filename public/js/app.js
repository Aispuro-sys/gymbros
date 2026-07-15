const API = '/api';
let token = localStorage.getItem('gym_bros_token');
let currentUser = null;
let exerciseLimit = 24;
let routineView = 'list';
let calendarMonth = new Date().getMonth();
let calendarYear = new Date().getFullYear();
let weeklyRoutines = [];
let allRoutinesWithGifs = [];

// ===== Theme =====
function initTheme() {
  const saved = localStorage.getItem('gym_bros_theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
}
function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('gym_bros_theme', next);
}
initTheme();

// ===== API Helper =====
async function apiCall(endpoint, method = 'GET', body = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const res = await fetch(`${API}${endpoint}`, {
    method, headers,
    body: body ? JSON.stringify(body) : null,
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

// ===== Auth =====
function showRegister() {
  document.getElementById('login-view').style.display = 'none';
  document.getElementById('register-view').style.display = 'block';
}
function showLogin() {
  document.getElementById('register-view').style.display = 'none';
  document.getElementById('login-view').style.display = 'block';
}
function showAuthError(id, msg) {
  const el = document.getElementById(id);
  el.textContent = msg; el.classList.add('show');
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  document.getElementById('login-error').classList.remove('show');
  try {
    const data = await apiCall('/auth/login', 'POST', {
      email: document.getElementById('login-email').value,
      password: document.getElementById('login-password').value,
    });
    token = data.token;
    localStorage.setItem('gym_bros_token', token);
    currentUser = data.user;
    showDashboard();
  } catch (err) { showAuthError('login-error', err.message); }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  document.getElementById('register-error').classList.remove('show');
  try {
    const data = await apiCall('/auth/register', 'POST', {
      username: document.getElementById('reg-username').value,
      email: document.getElementById('reg-email').value,
      password: document.getElementById('reg-password').value,
      age: parseInt(document.getElementById('reg-age').value) || undefined,
      height_cm: parseFloat(document.getElementById('reg-height').value) || undefined,
      weight_kg: parseFloat(document.getElementById('reg-weight').value) || undefined,
      goal: document.getElementById('reg-goal').value,
      body_type: document.getElementById('reg-body-type').value || undefined,
    });
    token = data.token;
    localStorage.setItem('gym_bros_token', token);
    currentUser = data.user;
    showDashboard();
  } catch (err) { showAuthError('register-error', err.message); }
});

function logout() {
  token = null; currentUser = null;
  localStorage.removeItem('gym_bros_token');
  document.getElementById('dashboard-page').style.display = 'none';
  document.getElementById('auth-page').style.display = 'flex';
}

// ===== Dashboard =====
async function showDashboard() {
  document.getElementById('auth-page').style.display = 'none';
  document.getElementById('dashboard-page').style.display = 'flex';
  try {
    const data = await apiCall('/auth/me');
    currentUser = data.user;
  } catch { logout(); return; }

  const u = currentUser;
  document.getElementById('user-name').textContent = u.username;
  document.getElementById('user-goal').textContent = goalLabel(u.goal);
  document.getElementById('user-avatar').textContent = u.username.charAt(0).toUpperCase();
  document.getElementById('today-date').textContent = new Date().toLocaleDateString('es-ES', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
  });
  checkAIStatus();
  navigateTo('overview');
}

function goalLabel(g) {
  return { BULKING: 'Volumen', CUTTING: 'Definición', MAINTENANCE: 'Mantener' }[g] || 'Mantener';
}
function timeLabel(t) {
  return { MORNING: 'Mañana', PRE_WORKOUT: 'Pre-entreno', NIGHT: 'Noche' }[t] || t;
}
function emptyState(text) {
  return `<div class="empty-state"><div class="empty-state-icon">—</div><div class="empty-state-text">${text}</div></div>`;
}
function loadingHtml() {
  return `<div class="loading"><div class="loading-spinner"></div><div>Generando...</div></div>`;
}

function navigateTo(page) {
  document.querySelectorAll('.page-section').forEach((s) => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach((n) => n.classList.remove('active'));
  document.querySelectorAll('.bottom-nav-item').forEach((n) => n.classList.remove('active'));
  document.getElementById(`page-${page}`).classList.add('active');
  const sidebarItem = document.querySelector(`.nav-item[data-page="${page}"]`);
  if (sidebarItem) sidebarItem.classList.add('active');
  const bottomItem = document.querySelector(`.bottom-nav-item[data-page="${page}"]`);
  if (bottomItem) bottomItem.classList.add('active');
  window.scrollTo(0, 0);
  if (page === 'overview') loadOverview();
  if (page === 'ai-coach') loadAICoach();
  if (page === 'routines') loadRoutines();
  if (page === 'exercises') loadExercises();
  if (page === 'nutrition') loadMacros();
  if (page === 'supplements') loadSupplements();
  if (page === 'teams') loadTeams();
  if (page === 'profile') loadProfile();
}

// ===== AI Status =====
async function checkAIStatus() {
  try {
    const data = await apiCall('/ai/status');
    const title = document.getElementById('ai-status-title');
    const desc = document.getElementById('ai-status-desc');
    if (data.ai_powered) {
      title.textContent = 'IA Coach — Activo (OpenAI)';
      desc.textContent = 'Conectado a GPT-4o. Rutinas generadas con IA real basadas en tu perfil.';
    } else {
      title.textContent = 'IA Coach — Modo local';
      desc.textContent = 'Generando rutinas con algoritmo inteligente basado en 1,324 ejercicios. Agrega OPENAI_API_KEY al .env para activar GPT-4o.';
    }
  } catch {}
}

// ===== Exercise GIF helper =====
function exerciseGifHtml(ex) {
  const gif = ex.gif_url ? `/exercises-dataset/${ex.gif_url}` : null;
  const img = ex.image ? `/exercises-dataset/${ex.image}` : null;
  const mediaSrc = gif || img;
  return `
    <div class="exercise-with-gif">
      ${mediaSrc ? `<img src="${mediaSrc}" alt="${ex.name}" class="exercise-gif" loading="lazy" />` : ''}
      <div class="exercise-info">
        <div class="exercise-name">${ex.name}</div>
        <div class="exercise-stats-row">
          <span>${ex.sets} series</span>
          <span>${ex.reps} reps</span>
          <span>${ex.rest_seconds}s descanso</span>
        </div>
      </div>
    </div>
  `;
}

// ===== Overview =====
async function loadOverview() {
  try {
    const [routinesData, macrosData, suppData] = await Promise.all([
      apiCall('/ai/routines-with-gifs'),
      apiCall(`/macros?date=${new Date().toISOString().split('T')[0]}`),
      apiCall('/supplements'),
    ]);
    const u = currentUser;
    const todayLog = macrosData.logs[0];
    const todayDow = new Date().getDay() === 0 ? 7 : new Date().getDay();
    const todayRoutine = routinesData.routines.find((r) => r.day_of_week === todayDow);
    document.getElementById('stats-grid').innerHTML = `
      <div class="stat-card"><div class="stat-label">Peso</div><div class="stat-value">${u.weight_kg || '--'}<span class="stat-unit"> kg</span></div></div>
      <div class="stat-card"><div class="stat-label">Calorías hoy</div><div class="stat-value">${todayLog ? todayLog.calories : 0}<span class="stat-unit"> kcal</span></div></div>
      <div class="stat-card"><div class="stat-label">Rutinas</div><div class="stat-value">${routinesData.routines.length}</div></div>
      <div class="stat-card"><div class="stat-label">Objetivo</div><div class="stat-value" style="font-size:1.3rem;">${goalLabel(u.goal)}</div></div>
    `;
    if (todayRoutine) {
      document.getElementById('overview-routine').innerHTML = `
        <div style="margin-bottom:8px;"><strong>${todayRoutine.name}</strong>
        <span class="list-item-badge badge-ai" style="margin-left:6px;">${todayRoutine.ai_generated ? 'IA' : 'Manual'}</span></div>
        ${todayRoutine.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
      `;
    } else {
      const latest = routinesData.routines[0];
      if (latest) {
        document.getElementById('overview-routine').innerHTML = `
          <div style="margin-bottom:8px;"><strong>${latest.name}</strong>
          <span class="list-item-badge ${latest.ai_generated ? 'badge-ai' : 'badge-manual'}" style="margin-left:6px;">${latest.ai_generated ? 'IA' : 'Manual'}</span></div>
          ${latest.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
        `;
      } else {
        document.getElementById('overview-routine').innerHTML = emptyState('Sin rutinas. Usa el IA Coach para crear un plan semanal.');
      }
    }
    if (todayLog) {
      document.getElementById('overview-macros').innerHTML = `
        <div class="exercise-row"><span class="exercise-name">Calorías</span><span class="exercise-stat">${todayLog.calories} kcal</span></div>
        <div class="exercise-row"><span class="exercise-name">Proteína</span><span class="exercise-stat">${todayLog.protein_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Carbs</span><span class="exercise-stat">${todayLog.carbs_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Grasas</span><span class="exercise-stat">${todayLog.fats_g}g</span></div>
      `;
    } else {
      document.getElementById('overview-macros').innerHTML = emptyState('Sin registro de macros hoy');
    }
    if (suppData.supplements.length > 0) {
      document.getElementById('overview-supplements').innerHTML = suppData.supplements.map((s) => `
        <div class="list-item"><div><div class="list-item-name">${s.name}</div><div class="list-item-meta">${s.dosage} · ${s.is_medication ? 'Medicina' : 'Suplemento'}</div></div>
        <span class="list-item-badge badge-${s.time_of_day.toLowerCase().replace('_', '-')}">${timeLabel(s.time_of_day)}</span></div>
      `).join('');
    } else {
      document.getElementById('overview-supplements').innerHTML = emptyState('Sin suplementos registrados');
    }
  } catch (err) { console.error('Overview error:', err); }
}

// ===== AI Coach =====
function loadAICoach() {
  document.getElementById('ai-weekly-result').innerHTML = '';
  document.getElementById('ai-routine-result').innerHTML = '';
  checkProfileComplete();
}

function checkProfileComplete() {
  const u = currentUser;
  const missing = [];
  if (!u.weight_kg) missing.push('peso');
  if (!u.height_cm) missing.push('altura');
  if (!u.goal) missing.push('objetivo');
  if (!u.body_type) missing.push('tipo de cuerpo');

  const banner = document.getElementById('ai-profile-warning');
  if (banner) banner.remove();

  if (missing.length > 0) {
    const html = `<div class="auth-error show" id="ai-profile-warning" style="margin-bottom:1rem;">
      Tu perfil está incompleto. Faltan: <strong>${missing.join(', ')}</strong>.
      <a onclick="navigateTo('profile')" style="color:var(--accent); cursor:pointer; text-decoration:underline; margin-left:6px;">Ir a Perfil</a>
    </div>`;
    const aiSection = document.getElementById('page-ai-coach');
    aiSection.insertAdjacentHTML('afterbegin', html);
    const btn = document.getElementById('ai-generate-btn');
    if (btn) { btn.disabled = true; btn.style.opacity = '0.5'; btn.textContent = 'Completa tu perfil primero'; }
  } else {
    const btn = document.getElementById('ai-generate-btn');
    if (btn) { btn.disabled = false; btn.style.opacity = '1'; btn.textContent = 'Generar plan semanal'; }
  }
  return missing.length === 0;
}

async function generateWeeklyPlan(e) {
  e.preventDefault();
  if (!checkProfileComplete()) return;
  const btn = document.getElementById('ai-generate-btn');
  btn.disabled = true; btn.textContent = 'Generando plan...';
  document.getElementById('ai-weekly-result').innerHTML = loadingHtml();
  try {
    const data = await apiCall('/ai/generate-weekly', 'POST', {
      days_per_week: parseInt(document.getElementById('ai-days').value),
      equipment: document.getElementById('ai-equipment').value,
      notes: document.getElementById('ai-notes').value,
    });
    document.getElementById('ai-weekly-result').innerHTML = `
      <div class="card" style="margin-top:1.5rem;">
        <div class="card-header">
          <div class="card-title">${data.plan_name}</div>
          <span class="list-item-badge badge-ai">${data.ai_powered ? 'GPT-4o' : 'Algoritmo local'}</span>
        </div>
        ${data.ai_notes ? `<div class="ai-notes"><strong>Coach IA:</strong> ${data.ai_notes}</div>` : ''}
      </div>
      ${data.days.map((day) => `
        <div class="routine-card">
          <div class="routine-header">
            <div>
              <div class="routine-name">${day.name}</div>
              <span class="list-item-badge badge-ai" style="margin-top:4px; display:inline-block;">Día ${day.day_of_week}</span>
            </div>
          </div>
          ${day.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
        </div>
      `).join('')}
    `;
    btn.textContent = 'Generar otro plan';
  } catch (err) {
    document.getElementById('ai-weekly-result').innerHTML = `<div class="auth-error show">${err.message}</div>`;
    btn.textContent = 'Generar plan semanal';
  }
  btn.disabled = false;
}

async function generateNutritionPlan() {
  if (!checkProfileComplete()) return;
  const container = document.getElementById('ai-nutrition-result');
  container.innerHTML = loadingHtml();
  try {
    const data = await apiCall('/ai/nutrition-plan', 'POST', {});
    const p = data.plan;
    let html = `
      <div class="exercise-row"><span class="exercise-name">Calorías objetivo</span><span class="exercise-stat">${p.calories} kcal</span></div>
      <div class="exercise-row"><span class="exercise-name">Proteína</span><span class="exercise-stat">${p.protein_g}g</span></div>
      <div class="exercise-row"><span class="exercise-name">Carbohidratos</span><span class="exercise-stat">${p.carbs_g}g</span></div>
      <div class="exercise-row"><span class="exercise-name">Grasas</span><span class="exercise-stat">${p.fats_g}g</span></div>
    `;
    if (p.meals && p.meals.length > 0) {
      html += `<div class="meal-section"><div class="meal-section-title">Comidas recomendadas</div>`;
      p.meals.forEach((meal) => {
        html += `
          <div class="meal-card">
            <div class="meal-header">
              <div class="meal-title">${meal.meal}</div>
              <div class="meal-macros">${meal.macros}</div>
            </div>
            <ul class="meal-foods">${meal.foods.map((f) => `<li>${f}</li>`).join('')}</ul>
          </div>
        `;
      });
      html += `</div>`;
    }
    if (p.notes) html += `<div class="ai-notes"><strong>Coach IA:</strong> ${p.notes}</div>`;
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<div class="auth-error show">${err.message}</div>`;
  }
}

// ===== Routines — Weekly/Monthly/List =====
function setRoutineView(view) {
  routineView = view;
  document.querySelectorAll('.view-toggle-btn').forEach((b) => b.classList.remove('active'));
  document.getElementById(`view-${view}-btn`).classList.add('active');
  document.getElementById('routine-weekly-view').style.display = view === 'week' ? 'block' : 'none';
  document.getElementById('routine-monthly-view').style.display = view === 'month' ? 'block' : 'none';
  document.getElementById('routine-list-view').style.display = view === 'list' ? 'block' : 'none';
  if (view === 'week') renderWeeklyCalendar();
  if (view === 'month') renderMonthlyCalendar();
}

async function loadRoutines() {
  try {
    const data = await apiCall('/ai/routines-with-gifs');
    allRoutinesWithGifs = data.routines;
    renderRoutinesList();
    if (routineView === 'week') renderWeeklyCalendar();
    if (routineView === 'month') renderMonthlyCalendar();
  } catch (err) { console.error('Routines error:', err); }
}

function renderRoutinesList() {
  const list = document.getElementById('routines-list');
  if (allRoutinesWithGifs.length === 0) {
    list.innerHTML = emptyState('Sin rutinas. Usa el IA Coach para generar un plan semanal.');
    return;
  }
  list.innerHTML = allRoutinesWithGifs.map((r) => `
    <div class="routine-card">
      <div class="routine-header">
        <div>
          <div class="routine-name">${r.name}</div>
          <span class="list-item-badge ${r.ai_generated ? 'badge-ai' : 'badge-manual'}" style="margin-top:4px; display:inline-block;">${r.ai_generated ? 'IA' : 'Manual'}</span>
        </div>
        <button class="logout-btn" onclick="deleteRoutine('${r.id}')" title="Eliminar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
      ${r.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
    </div>
  `).join('');
}

const DAY_NAMES = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const DAY_NAMES_FULL = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

function renderWeeklyCalendar() {
  const today = new Date();
  const todayDow = today.getDay() === 0 ? 7 : today.getDay();
  const monday = new Date(today);
  monday.setDate(today.getDate() - todayDow + 1);

  const routineByDay = {};
  allRoutinesWithGifs.forEach((r) => {
    if (r.day_of_week) routineByDay[r.day_of_week] = r;
  });

  const html = DAY_NAMES.map((dayName, i) => {
    const dow = i + 1;
    const date = new Date(monday);
    date.setDate(monday.getDate() + i);
    const isToday = dow === todayDow;
    const routine = routineByDay[dow];
    const isRest = !routine;

    return `
      <div class="week-day ${isToday ? 'active' : ''} ${isRest ? 'rest' : ''}" onclick="showDayDetail(${dow})">
        <div class="week-day-header">
          <span>${dayName}</span>
          <span class="day-num">${date.getDate()}</span>
        </div>
        ${routine ? `
          <div class="week-day-focus">${routine.name.split(' - ')[1] || routine.name}</div>
          <div class="week-day-meta">${routine.exercises.length} ejercicios</div>
          <span class="week-day-badge">${routine.ai_generated ? 'IA' : 'Manual'}</span>
        ` : `
          <div class="week-day-focus">Descanso</div>
          <span class="week-day-badge rest-badge">Rest</span>
        `}
      </div>
    `;
  }).join('');

  document.getElementById('weekly-calendar').innerHTML = html;
  showDayDetail(todayDow);
}

function showDayDetail(dow) {
  const routine = allRoutinesWithGifs.find((r) => r.day_of_week === dow);
  const container = document.getElementById('week-day-detail');
  if (!routine) {
    container.innerHTML = `
      <div class="day-detail">
        <div class="day-detail-header">
          <div class="day-detail-title">${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">Día de descanso</div>
        </div>
        <div class="empty-state"><div class="empty-state-text">Recupérate. Mañana se entrena.</div></div>
      </div>
    `;
    return;
  }
  container.innerHTML = `
    <div class="day-detail">
      <div class="day-detail-header">
        <div>
          <div class="day-detail-title">${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">${routine.name}</div>
        </div>
        <span class="list-item-badge ${routine.ai_generated ? 'badge-ai' : 'badge-manual'}">${routine.ai_generated ? 'IA' : 'Manual'}</span>
      </div>
      ${routine.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
    </div>
  `;
}

function renderMonthlyCalendar() {
  const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  document.getElementById('monthly-title').textContent = `${monthNames[calendarMonth]} ${calendarYear}`;

  const firstDay = new Date(calendarYear, calendarMonth, 1);
  const lastDay = new Date(calendarYear, calendarMonth + 1, 0);
  let startDow = firstDay.getDay() === 0 ? 7 : firstDay.getDay();
  startDow -= 1;

  const today = new Date();
  const isCurrentMonth = today.getMonth() === calendarMonth && today.getFullYear() === calendarYear;

  const routineByDay = {};
  allRoutinesWithGifs.forEach((r) => {
    if (r.day_of_week) routineByDay[r.day_of_week] = r;
  });

  let html = DAY_NAMES.map((d) => `<div class="monthly-day-header">${d}</div>`).join('');

  for (let i = 0; i < startDow; i++) {
    html += `<div class="monthly-cell empty"></div>`;
  }

  for (let d = 1; d <= lastDay.getDate(); d++) {
    const date = new Date(calendarYear, calendarMonth, d);
    const dow = date.getDay() === 0 ? 7 : date.getDay();
    const hasRoutine = !!routineByDay[dow];
    const isToday = isCurrentMonth && d === today.getDate();

    html += `
      <div class="monthly-cell ${isToday ? 'today' : ''} ${hasRoutine ? 'has-routine' : 'rest-day'}" onclick="showMonthDayDetail(${dow}, ${d})">
        <div class="day-num">${d}</div>
        <div class="day-dot"></div>
      </div>
    `;
  }

  document.getElementById('monthly-grid').innerHTML = html;
}

function showMonthDayDetail(dow, dayNum) {
  const routine = allRoutinesWithGifs.find((r) => r.day_of_week === dow);
  const container = document.getElementById('month-day-detail');
  if (!routine) {
    container.innerHTML = `
      <div class="day-detail">
        <div class="day-detail-header">
          <div class="day-detail-title">${dayNum} — ${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">Día de descanso</div>
        </div>
      </div>
    `;
    return;
  }
  container.innerHTML = `
    <div class="day-detail">
      <div class="day-detail-header">
        <div>
          <div class="day-detail-title">${dayNum} — ${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">${routine.name}</div>
        </div>
        <span class="list-item-badge ${routine.ai_generated ? 'badge-ai' : 'badge-manual'}">${routine.ai_generated ? 'IA' : 'Manual'}</span>
      </div>
      ${routine.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
    </div>
  `;
}

function prevMonth() {
  calendarMonth--;
  if (calendarMonth < 0) { calendarMonth = 11; calendarYear--; }
  renderMonthlyCalendar();
}
function nextMonth() {
  calendarMonth++;
  if (calendarMonth > 11) { calendarMonth = 0; calendarYear++; }
  renderMonthlyCalendar();
}

async function deleteRoutine(id) {
  if (!confirm('¿Eliminar rutina?')) return;
  try {
    await apiCall(`/routines/${id}`, 'DELETE');
    loadRoutines();
  } catch (err) { alert(err.message); }
}

function openRoutineModal() {
  showModal(`
    <div class="modal-header"><div class="modal-title">Nueva rutina</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
    <form onsubmit="createRoutine(event)">
      <div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="routine-name" placeholder="Push Day" required /></div>
      <div id="exercises-container"></div>
      <button type="button" class="btn-secondary" style="margin-bottom:0.75rem;" onclick="addExerciseField()">+ Ejercicio</button>
      <button type="submit" class="btn-primary">Crear</button>
    </form>
  `);
  addExerciseField();
}

function addExerciseField() {
  const container = document.getElementById('exercises-container');
  const index = container.children.length;
  const div = document.createElement('div');
  div.style.cssText = 'border:1px solid var(--border); border-radius:6px; padding:0.75rem; margin-bottom:0.5rem;';
  div.innerHTML = `
    <div class="form-group"><label>Ejercicio ${index + 1}</label><input type="text" class="form-input ex-name" placeholder="Press Banca" required /></div>
    <div class="form-row">
      <div class="form-group"><label>Series</label><input type="number" class="form-input ex-sets" value="4" min="1" /></div>
      <div class="form-group"><label>Reps</label><input type="text" class="form-input ex-reps" value="8-12" /></div>
      <div class="form-group"><label>Descanso</label><input type="number" class="form-input ex-rest" value="90" min="0" /></div>
    </div>
  `;
  container.appendChild(div);
}

async function createRoutine(e) {
  e.preventDefault();
  const exercises = [];
  document.querySelectorAll('#exercises-container > div').forEach((div) => {
    exercises.push({
      name: div.querySelector('.ex-name').value,
      sets: parseInt(div.querySelector('.ex-sets').value) || 3,
      reps: div.querySelector('.ex-reps').value || '8-12',
      rest_seconds: parseInt(div.querySelector('.ex-rest').value) || 90,
    });
  });
  try {
    await apiCall('/routines', 'POST', {
      name: document.getElementById('routine-name').value, ai_generated: false, exercises,
    });
    closeModal(); loadRoutines();
  } catch (err) { alert(err.message); }
}

// ===== Exercise Browser =====
async function loadExercises() {
  try {
    const cats = await apiCall('/exercises/categories');
    const select = document.getElementById('exercise-filter-category');
    select.innerHTML = '<option value="">Todas las categorías</option>' +
      cats.categories.map((c) => `<option value="${c.value}">${c.label}</option>`).join('');
    searchExercises();
  } catch (err) { console.error('Exercise categories error:', err); }
}

async function searchExercises() {
  const q = document.getElementById('exercise-search').value;
  const category = document.getElementById('exercise-filter-category').value;
  const grid = document.getElementById('exercises-grid');
  try {
    const data = await apiCall(`/exercises?q=${encodeURIComponent(q)}&category=${encodeURIComponent(category)}&limit=${exerciseLimit}`);
    grid.innerHTML = data.exercises.map((ex) => `
      <div class="exercise-tile" onclick="viewExercise('${ex.id}')">
        <img src="/exercises-dataset/${ex.image}" alt="${ex.name}" loading="lazy" />
        <div class="exercise-tile-name">${ex.name}</div>
        <div class="exercise-tile-meta">${ex.category_es} · ${ex.equipment_es}</div>
      </div>
    `).join('');
    document.getElementById('load-more-btn').style.display = data.exercises.length >= exerciseLimit ? 'inline-block' : 'none';
  } catch (err) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-state-text">Error cargando ejercicios</div></div>`;
  }
}

function loadMoreExercises() { exerciseLimit += 24; searchExercises(); }

function viewExercise(id) {
  apiCall(`/exercises/${id}`).then((data) => {
    const ex = data.exercise;
    const steps = ex.instruction_steps && ex.instruction_steps.es ? ex.instruction_steps.es : [];
    showModal(`
      <div class="modal-header"><div class="modal-title">${ex.name}</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
      <div style="text-align:center; margin-bottom:1rem;">
        <img src="/exercises-dataset/${ex.gif_url}" alt="${ex.name}" style="width:180px; height:180px; border-radius:8px; border:1px solid var(--border);" />
      </div>
      <div style="display:flex; gap:6px; flex-wrap:wrap; margin-bottom:1rem;">
        <span class="list-item-badge badge-manual">${ex.category_es}</span>
        <span class="list-item-badge badge-manual">${ex.equipment_es}</span>
        <span class="list-item-badge badge-manual">${ex.target_es}</span>
        ${ex.muscle_group_es ? `<span class="list-item-badge badge-manual">${ex.muscle_group_es}</span>` : ''}
      </div>
      ${steps.length > 0 ? `
        <div style="margin-bottom:1rem;">
          <div style="font-size:0.75rem; font-weight:600; color:var(--text-2); text-transform:uppercase; letter-spacing:0.5px; margin-bottom:0.5rem;">Instrucciones</div>
          <ol style="padding-left:1.25rem; font-size:0.85rem; color:var(--text-2); line-height:1.6;">
            ${steps.map((s) => `<li style="margin-bottom:0.4rem;">${s}</li>`).join('')}
          </ol>
        </div>
      ` : ''}
      ${ex.secondary_muscles_es && ex.secondary_muscles_es.length > 0 ? `<div style="font-size:0.8rem; color:var(--text-3);">Músculos secundarios: ${ex.secondary_muscles_es.join(', ')}</div>` : ''}
    `);
  }).catch((err) => alert(err.message));
}

// ===== Macros =====
async function loadMacros() {
  try {
    const data = await apiCall('/macros');
    const list = document.getElementById('macros-list');
    if (data.logs.length === 0) {
      list.innerHTML = emptyState('Sin registros. Registra tu primer día.');
      return;
    }
    list.innerHTML = data.logs.map((l) => `
      <div class="list-item"><div>
        <div class="list-item-name">${new Date(l.date).toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' })}</div>
        <div class="list-item-meta">${l.calories} kcal · P:${l.protein_g}g · C:${l.carbs_g}g · G:${l.fats_g}g</div>
      </div></div>
    `).join('');
  } catch (err) { console.error('Macros error:', err); }
}

function openMacrosModal() {
  const today = new Date().toISOString().split('T')[0];
  showModal(`
    <div class="modal-header"><div class="modal-title">Registrar macros</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
    <form onsubmit="saveMacros(event)">
      <div class="form-group"><label>Fecha</label><input type="date" class="form-input" id="macro-date" value="${today}" required /></div>
      <div class="form-row">
        <div class="form-group"><label>Calorías</label><input type="number" class="form-input" id="macro-calories" placeholder="2500" min="0" /></div>
        <div class="form-group"><label>Proteína (g)</label><input type="number" class="form-input" id="macro-protein" placeholder="180" min="0" /></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Carbs (g)</label><input type="number" class="form-input" id="macro-carbs" placeholder="300" min="0" /></div>
        <div class="form-group"><label>Grasas (g)</label><input type="number" class="form-input" id="macro-fats" placeholder="70" min="0" /></div>
      </div>
      <button type="submit" class="btn-primary">Guardar</button>
    </form>
  `);
}

async function saveMacros(e) {
  e.preventDefault();
  try {
    await apiCall('/macros', 'POST', {
      date: document.getElementById('macro-date').value,
      calories: parseInt(document.getElementById('macro-calories').value) || 0,
      protein_g: parseInt(document.getElementById('macro-protein').value) || 0,
      carbs_g: parseInt(document.getElementById('macro-carbs').value) || 0,
      fats_g: parseInt(document.getElementById('macro-fats').value) || 0,
    });
    closeModal(); loadMacros();
  } catch (err) { alert(err.message); }
}

// ===== Meal recommendations in nutrition page =====
async function loadMealRecommendations() {
  if (!checkProfileComplete()) {
    const section = document.getElementById('nutrition-meals-section');
    section.style.display = 'block';
    section.innerHTML = `<div class="auth-error show">Tu perfil está incompleto. Ve a Perfil para registrar tus datos primero. <a onclick="navigateTo('profile')" style="color:var(--accent); cursor:pointer; text-decoration:underline; margin-left:6px;">Ir a Perfil</a></div>`;
    return;
  }
  const section = document.getElementById('nutrition-meals-section');
  section.style.display = 'block';
  section.innerHTML = loadingHtml();
  try {
    const data = await apiCall('/ai/nutrition-plan', 'POST', {});
    const p = data.plan;
    let html = `
      <div class="card" style="margin-bottom:1rem;">
        <div class="card-header"><div class="card-title">Macros objetivo — ${goalLabel(currentUser.goal)}</div></div>
        <div class="exercise-row"><span class="exercise-name">Calorías</span><span class="exercise-stat">${p.calories} kcal</span></div>
        <div class="exercise-row"><span class="exercise-name">Proteína</span><span class="exercise-stat">${p.protein_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Carbs</span><span class="exercise-stat">${p.carbs_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Grasas</span><span class="exercise-stat">${p.fats_g}g</span></div>
      </div>
      <div class="meal-section-title">Comidas recomendadas para ${goalLabel(currentUser.goal).toLowerCase()}</div>
    `;
    p.meals.forEach((meal) => {
      html += `
        <div class="meal-card">
          <div class="meal-header"><div class="meal-title">${meal.meal}</div><div class="meal-macros">${meal.macros}</div></div>
          <ul class="meal-foods">${meal.foods.map((f) => `<li>${f}</li>`).join('')}</ul>
        </div>
      `;
    });
    if (p.notes) html += `<div class="ai-notes"><strong>Coach IA:</strong> ${p.notes}</div>`;
    section.innerHTML = html;
  } catch (err) {
    section.innerHTML = `<div class="auth-error show">${err.message}</div>`;
  }
}

// ===== Supplements =====
async function loadSupplements() {
  try {
    const data = await apiCall('/supplements');
    const list = document.getElementById('supplements-list');
    if (data.supplements.length === 0) {
      list.innerHTML = emptyState('Sin suplementos registrados.');
      return;
    }
    list.innerHTML = data.supplements.map((s) => `
      <div class="list-item"><div><div class="list-item-name">${s.name}</div><div class="list-item-meta">${s.dosage} · ${s.is_medication ? 'Medicina' : 'Suplemento'}</div></div>
      <div style="display:flex; align-items:center; gap:10px;">
        <span class="list-item-badge badge-${s.time_of_day.toLowerCase().replace('_', '-')}">${timeLabel(s.time_of_day)}</span>
        <button class="logout-btn" onclick="deleteSupplement('${s.id}')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
      </div></div>
    `).join('');
  } catch (err) { console.error('Supplements error:', err); }
}

async function deleteSupplement(id) {
  if (!confirm('¿Eliminar?')) return;
  try { await apiCall(`/supplements/${id}`, 'DELETE'); loadSupplements(); } catch (err) { alert(err.message); }
}

function openSupplementModal() {
  showModal(`
    <div class="modal-header"><div class="modal-title">Agregar suplemento</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
    <form onsubmit="createSupplement(event)">
      <div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="supp-name" placeholder="Creatina monohidratada" required /></div>
      <div class="form-group"><label>Dosis</label><input type="text" class="form-input" id="supp-dosage" placeholder="5g" required /></div>
      <div class="form-row">
        <div class="form-group"><label>Momento</label><select class="form-input" id="supp-time"><option value="MORNING">Mañana</option><option value="PRE_WORKOUT">Pre-entreno</option><option value="NIGHT">Noche</option></select></div>
        <div class="form-group"><label>Tipo</label><select class="form-input" id="supp-med"><option value="false">Suplemento</option><option value="true">Medicina</option></select></div>
      </div>
      <button type="submit" class="btn-primary">Agregar</button>
    </form>
  `);
}

async function createSupplement(e) {
  e.preventDefault();
  try {
    await apiCall('/supplements', 'POST', {
      name: document.getElementById('supp-name').value,
      dosage: document.getElementById('supp-dosage').value,
      time_of_day: document.getElementById('supp-time').value,
      is_medication: document.getElementById('supp-med').value === 'true',
    });
    closeModal(); loadSupplements();
  } catch (err) { alert(err.message); }
}

// ===== Teams =====
async function loadTeams() {
  try {
    const data = await apiCall('/teams');
    const list = document.getElementById('teams-list');
    if (data.teams.length === 0) {
      list.innerHTML = emptyState('No perteneces a ningún equipo.');
      return;
    }
    list.innerHTML = data.teams.map((t) => `
      <div class="routine-card">
        <div class="routine-header"><div>
          <div class="routine-name">${t.name}</div>
          <div class="list-item-meta">Código: <strong style="color:var(--text);">${t.invite_code}</strong> · ${t.members.length} miembro(s)</div>
        </div>
        <span class="list-item-badge ${t.role === 'ADMIN' ? 'badge-ai' : 'badge-manual'}">${t.role === 'ADMIN' ? 'Admin' : 'Miembro'}</span></div>
        <div style="margin-top:0.5rem;">${t.members.map((m) => `<div class="list-item-name" style="padding:5px 0; border-bottom:1px solid var(--border);">${m.user.username}${m.role === 'ADMIN' ? ' *' : ''}</div>`).join('')}</div>
      </div>
    `).join('');
  } catch (err) { console.error('Teams error:', err); }
}

function openCreateTeamModal() {
  showModal(`<div class="modal-header"><div class="modal-title">Crear equipo</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
  <form onsubmit="createTeam(event)"><div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="team-name" placeholder="Los Bros" required /></div>
  <button type="submit" class="btn-primary">Crear</button></form>`);
}
async function createTeam(e) {
  e.preventDefault();
  try { await apiCall('/teams', 'POST', { name: document.getElementById('team-name').value }); closeModal(); loadTeams(); } catch (err) { alert(err.message); }
}

function openJoinTeamModal() {
  showModal(`<div class="modal-header"><div class="modal-title">Unirse a equipo</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
  <form onsubmit="joinTeam(event)"><div class="form-group"><label>Código</label><input type="text" class="form-input" id="team-code" placeholder="GYM-ABC123" required style="text-transform:uppercase;" /></div>
  <button type="submit" class="btn-primary">Unirse</button></form>`);
}
async function joinTeam(e) {
  e.preventDefault();
  try { await apiCall('/teams/join', 'POST', { invite_code: document.getElementById('team-code').value.toUpperCase() }); closeModal(); loadTeams(); } catch (err) { alert(err.message); }
}

// ===== Photo Capture: Food & Supplements =====
function openFoodCamera() {
  const section = document.getElementById('food-photo-section');
  section.style.display = 'block';
  section.innerHTML = `
    <div class="card photo-capture-card">
      <div class="card-header"><div class="card-title">Analizar comida con IA</div><button class="modal-close" onclick="document.getElementById('food-photo-section').style.display='none'">&times;</button></div>
      <p style="font-size:0.8rem; color:var(--text-2); margin-bottom:1rem;">Toma una foto de tu comida y la IA estimara calorias, proteinas, carbos y grasas.</p>
      <div class="photo-capture-area">
        <input type="file" id="food-file-input" accept="image/*" capture="environment" style="display:none;" onchange="handleFoodPhoto(event)" />
        <button class="btn-primary photo-capture-btn" onclick="document.getElementById('food-file-input').click()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
          Tomar foto / Subir imagen
        </button>
      </div>
      <div id="food-photo-preview" style="display:none;"></div>
      <div id="food-analysis-result"></div>
    </div>
  `;
}

async function handleFoodPhoto(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = async (e) => {
    const dataUrl = e.target.result;
    document.getElementById('food-photo-preview').style.display = 'block';
    document.getElementById('food-photo-preview').innerHTML = `<img src="${dataUrl}" class="photo-preview-img" />`;
    document.getElementById('food-analysis-result').innerHTML = loadingHtml();

    try {
      const data = await apiCall('/ai/analyze-food', 'POST', { image: dataUrl });
      const a = data.analysis;
      const confidenceColor = a.confidence === 'high' ? 'var(--text)' : a.confidence === 'medium' ? 'var(--text-2)' : 'var(--danger)';
      document.getElementById('food-analysis-result').innerHTML = `
        <div class="analysis-result-card">
          <div class="analysis-header">
            <div class="analysis-name">${a.food_name}</div>
            <span class="analysis-badge" style="color:${confidenceColor}; border-color:${confidenceColor};">${a.confidence || 'low'}</span>
          </div>
          <div class="analysis-portion">${a.estimated_portion || ''}</div>
          <div class="analysis-macros-grid">
            <div class="macro-pill"><div class="macro-pill-val">${a.calories}</div><div class="macro-pill-label">kcal</div></div>
            <div class="macro-pill"><div class="macro-pill-val">${a.protein_g}g</div><div class="macro-pill-label">Proteina</div></div>
            <div class="macro-pill"><div class="macro-pill-val">${a.carbs_g}g</div><div class="macro-pill-label">Carbos</div></div>
            <div class="macro-pill"><div class="macro-pill-val">${a.fats_g}g</div><div class="macro-pill-label">Grasas</div></div>
          </div>
          ${a.fiber_g != null ? `<div class="analysis-extra">Fibra: ${a.fiber_g}g</div>` : ''}
          ${a.sugar_g != null ? `<div class="analysis-extra">Azucar: ${a.sugar_g}g</div>` : ''}
          ${a.sodium_mg != null ? `<div class="analysis-extra">Sodio: ${a.sodium_mg}mg</div>` : ''}
          ${a.notes ? `<div class="analysis-notes">${a.notes}</div>` : ''}
          <div style="display:flex; gap:8px; margin-top:0.75rem;">
            <button class="btn-primary" style="width:auto; padding:8px 16px;" onclick="saveFoodAsMacros(${a.calories}, ${a.protein_g}, ${a.carbs_g}, ${a.fats_g})">Registrar macros</button>
            <button class="btn-secondary" style="width:auto; padding:8px 16px;" onclick="openFoodCamera()">Otra foto</button>
          </div>
        </div>
      `;
    } catch (err) {
      document.getElementById('food-analysis-result').innerHTML = `<div class="auth-error show">${err.message}</div>`;
    }
  };
  reader.readAsDataURL(file);
}

function saveFoodAsMacros(cal, protein, carbs, fats) {
  const today = new Date().toISOString().split('T')[0];
  apiCall('/macros', 'POST', { date: today, calories: cal, protein_g: protein, carbs_g: carbs, fats_g: fats })
    .then(() => { alert('Macros registrados'); document.getElementById('food-photo-section').style.display = 'none'; loadMacros(); })
    .catch((err) => alert(err.message));
}

function openSupplementCamera() {
  const section = document.getElementById('supplement-photo-section');
  section.style.display = 'block';
  section.innerHTML = `
    <div class="card photo-capture-card">
      <div class="card-header"><div class="card-title">Analizar etiqueta con IA</div><button class="modal-close" onclick="document.getElementById('supplement-photo-section').style.display='none'">&times;</button></div>
      <p style="font-size:0.8rem; color:var(--text-2); margin-bottom:1rem;">Toma una foto del frasco o etiqueta nutricional y la IA extraera la informacion del suplemento.</p>
      <div class="photo-capture-area">
        <input type="file" id="supp-file-input" accept="image/*" capture="environment" style="display:none;" onchange="handleSupplementPhoto(event)" />
        <button class="btn-primary photo-capture-btn" onclick="document.getElementById('supp-file-input').click()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
          Tomar foto / Subir imagen
        </button>
      </div>
      <div id="supp-photo-preview" style="display:none;"></div>
      <div id="supp-analysis-result"></div>
    </div>
  `;
}

async function handleSupplementPhoto(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = async (e) => {
    const dataUrl = e.target.result;
    document.getElementById('supp-photo-preview').style.display = 'block';
    document.getElementById('supp-photo-preview').innerHTML = `<img src="${dataUrl}" class="photo-preview-img" />`;
    document.getElementById('supp-analysis-result').innerHTML = loadingHtml();

    try {
      const data = await apiCall('/ai/analyze-supplement', 'POST', { image: dataUrl });
      const a = data.analysis;
      const confidenceColor = a.confidence === 'high' ? 'var(--text)' : a.confidence === 'medium' ? 'var(--text-2)' : 'var(--danger)';
      document.getElementById('supp-analysis-result').innerHTML = `
        <div class="analysis-result-card">
          <div class="analysis-header">
            <div class="analysis-name">${a.name}</div>
            <span class="analysis-badge" style="color:${confidenceColor}; border-color:${confidenceColor};">${a.confidence || 'low'}</span>
          </div>
          ${a.brand ? `<div class="analysis-brand">${a.brand}</div>` : ''}
          <div class="analysis-info-grid">
            <div class="analysis-info-item"><span>Categoria</span><strong>${a.category}</strong></div>
            <div class="analysis-info-item"><span>Porcion</span><strong>${a.serving_size}</strong></div>
            ${a.servings_per_container ? `<div class="analysis-info-item"><span>Porciones</span><strong>${a.servings_per_container}</strong></div>` : ''}
            ${a.calories ? `<div class="analysis-info-item"><span>Calorias</span><strong>${a.calories} kcal</strong></div>` : ''}
            ${a.protein_g ? `<div class="analysis-info-item"><span>Proteina</span><strong>${a.protein_g}g</strong></div>` : ''}
            ${a.carbs_g ? `<div class="analysis-info-item"><span>Carbos</span><strong>${a.carbs_g}g</strong></div>` : ''}
            ${a.fats_g ? `<div class="analysis-info-item"><span>Grasas</span><strong>${a.fats_g}g</strong></div>` : ''}
          </div>
          ${a.key_ingredients && a.key_ingredients.length > 0 ? `<div class="analysis-ingredients"><strong>Ingredientes:</strong> ${a.key_ingredients.join(', ')}</div>` : ''}
          ${a.dose_per_serving && a.dose_per_serving !== 'N/A' ? `<div class="analysis-extra">Dosis: ${a.dose_per_serving}</div>` : ''}
          ${a.usage_instructions ? `<div class="analysis-extra">Uso: ${a.usage_instructions}</div>` : ''}
          ${a.warnings ? `<div class="analysis-extra" style="color:var(--danger);">Advertencias: ${a.warnings}</div>` : ''}
          ${a.notes ? `<div class="analysis-notes">${a.notes}</div>` : ''}
          <div style="display:flex; gap:8px; margin-top:0.75rem;">
            <button class="btn-primary" style="width:auto; padding:8px 16px;" onclick='saveSupplementFromPhoto(${JSON.stringify(a).replace(/'/g, "&#39;")})'>Agregar suplemento</button>
            <button class="btn-secondary" style="width:auto; padding:8px 16px;" onclick="openSupplementCamera()">Otra foto</button>
          </div>
        </div>
      `;
    } catch (err) {
      document.getElementById('supp-analysis-result').innerHTML = `<div class="auth-error show">${err.message}</div>`;
    }
  };
  reader.readAsDataURL(file);
}

function saveSupplementFromPhoto(analysis) {
  const name = analysis.name || 'Suplemento';
  const dosage = analysis.dose_per_serving || analysis.serving_size || '';
  const timeOfDay = 'MORNING';
  apiCall('/supplements', 'POST', { name, dosage, time_of_day: timeOfDay, is_medication: false })
    .then(() => { alert('Suplemento agregado'); document.getElementById('supplement-photo-section').style.display = 'none'; loadSupplements(); })
    .catch((err) => alert(err.message));
}

// ===== Modal =====
function showModal(html) {
  document.getElementById('modal-content').innerHTML = html;
  document.getElementById('modal-overlay').classList.add('show');
}
function closeModal() { document.getElementById('modal-overlay').classList.remove('show'); }
document.getElementById('modal-overlay').addEventListener('click', (e) => { if (e.target.id === 'modal-overlay') closeModal(); });

// ===== Body Type Selection =====
function selectBodyType(prefix, type) {
  const grid = document.getElementById(`${prefix}-body-type-grid`);
  grid.querySelectorAll('.body-type-card').forEach((c) => c.classList.remove('selected'));
  grid.querySelector(`[data-body-type="${type}"]`).classList.add('selected');
  document.getElementById(`${prefix}-body-type`).value = type;
}

// ===== Profile =====
function loadProfile() {
  const u = currentUser;
  document.getElementById('prof-username').value = u.username || '';
  document.getElementById('prof-email').value = u.email || '';
  document.getElementById('prof-age').value = u.age || '';
  document.getElementById('prof-height').value = u.height_cm || '';
  document.getElementById('prof-weight').value = u.weight_kg || '';
  document.getElementById('prof-goal').value = u.goal || 'MAINTENANCE';

  if (u.body_type) {
    selectBodyType('prof', u.body_type);
  }

  const bmiCard = document.getElementById('profile-bmi-card');
  if (u.weight_kg && u.height_cm) {
    const bmi = (u.weight_kg / Math.pow(u.height_cm / 100, 2));
    const bmiCat = bmi < 18.5 ? 'Bajo peso' : bmi < 25 ? 'Peso normal' : bmi < 30 ? 'Sobrepeso' : 'Obesidad';
    bmiCard.style.display = 'block';
    document.getElementById('profile-bmi-content').innerHTML = `
      <div class="exercise-row"><span class="exercise-name">IMC</span><span class="exercise-stat">${bmi.toFixed(1)}</span></div>
      <div class="exercise-row"><span class="exercise-name">Categoría</span><span class="exercise-stat">${bmiCat}</span></div>
      <div class="exercise-row"><span class="exercise-name">Peso</span><span class="exercise-stat">${u.weight_kg} kg</span></div>
      <div class="exercise-row"><span class="exercise-name">Altura</span><span class="exercise-stat">${u.height_cm} cm</span></div>
      ${u.body_type ? `<div class="exercise-row"><span class="exercise-name">Somatotipo</span><span class="exercise-stat">${bodyTypeLabel(u.body_type)}</span></div>` : ''}
    `;
  } else {
    bmiCard.style.display = 'none';
  }
}

function bodyTypeLabel(t) {
  return { ECTOMORPH: 'Ectomorfo', MESOMORPH: 'Mesomorfo', ENDOMORPH: 'Endomorfo' }[t] || 'No definido';
}

async function saveProfile(e) {
  e.preventDefault();
  try {
    const data = await apiCall('/auth/profile', 'PUT', {
      username: document.getElementById('prof-username').value,
      age: parseInt(document.getElementById('prof-age').value) || undefined,
      height_cm: parseFloat(document.getElementById('prof-height').value) || undefined,
      weight_kg: parseFloat(document.getElementById('prof-weight').value) || undefined,
      goal: document.getElementById('prof-goal').value,
    });
    currentUser = data.user;
    document.getElementById('user-name').textContent = data.user.username;
    document.getElementById('user-goal').textContent = goalLabel(data.user.goal);
    loadProfile();
    alert('Perfil actualizado');
  } catch (err) { alert(err.message); }
}

async function saveBodyType() {
  const bodyType = document.getElementById('prof-body-type').value;
  if (!bodyType) { alert('Selecciona un tipo de cuerpo'); return; }
  try {
    const data = await apiCall('/auth/profile', 'PUT', { body_type: bodyType });
    currentUser = data.user;
    alert('Tipo de cuerpo guardado: ' + bodyTypeLabel(bodyType));
  } catch (err) { alert(err.message); }
}

// ===== Init =====
if (token) { showDashboard(); }
