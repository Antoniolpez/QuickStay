function q(s) { return document.querySelector(s); }

const tabs = Array.from(document.querySelectorAll('.tab'));
const panels = {
  overview: q('#panel-overview'),
  ports: q('#panel-ports'),
  vpn: q('#panel-vpn'),
  devices: q('#panel-devices'),
  nft: q('#panel-nft'),
  ipt: q('#panel-ipt'),
  routes: q('#panel-routes'),
  apply: q('#panel-apply')
};

let timer = null;
let devicesActiveMode = false;

const KNOWN_IP_DESCRIPTIONS = {
  '172.16.10.1': 'Gateway Docker DMZ (bridge)',
  '172.16.10.2': 'Router FW en DMZ',
  '172.16.10.3': 'Router FW IP secundaria DMZ',
  '172.16.10.10': 'HAProxy / Load Balancer',
  '172.16.10.20': 'Web Server 1',
  '172.16.10.21': 'Web Server 2',
  '172.16.20.1': 'Gateway Docker APP (bridge)',
  '172.16.20.2': 'Router FW en APP',
  '172.16.20.3': 'Router FW IP secundaria APP',
  '172.16.20.10': 'Servidor Aplicacion',
  '172.16.20.20': 'MySQL',
  '172.16.30.1': 'Gateway Docker MGMT (bridge)',
  '172.16.30.2': 'Router FW en MGMT',
  '172.16.30.3': 'Router FW IP secundaria MGMT',
  '172.16.30.10': 'AD-DC / DNS interno',
  '172.16.30.11': 'AD-DC secundario o alias MGMT',
  '172.16.30.20': 'Zabbix Web',
  '172.16.30.21': 'Grafana',
  '172.16.30.22': 'Zabbix Server',
  '172.16.30.23': 'Zabbix DB',
  '172.16.30.30': 'Wazuh Manager',
  '172.16.30.31': 'Wazuh Dashboard',
  '172.16.30.32': 'Wazuh Indexer',
  '172.16.30.40': 'Dashboard Backend',
  '172.16.40.1': 'Gateway Docker IoT (bridge)',
  '172.16.40.2': 'Router FW en IoT',
  '172.16.40.3': 'Router FW IP secundaria IoT',
  '172.16.40.4': 'Router FW IP terciaria IoT',
  '172.16.40.10': 'Broker MQTT',
  '172.16.50.1': 'Gateway Docker VPN (bridge)',
  '172.16.50.2': 'Router FW en VPN'
};

function setText(el, txt) { if (el) el.textContent = txt || ''; }
function pretty(v) { return typeof v === 'string' ? v : JSON.stringify(v, null, 2); }

function api(path, opts, done) {
  opts = opts || {};
  const xhr = new XMLHttpRequest();
  xhr.open(opts.method || 'GET', path, true);
  xhr.timeout = Number(opts.timeout || 5000);
  xhr.setRequestHeader('Accept', 'application/json, text/plain, */*');
  if (opts.headers) {
    Object.keys(opts.headers).forEach((k) => xhr.setRequestHeader(k, opts.headers[k]));
  }
  xhr.onreadystatechange = function () {
    if (xhr.readyState !== 4) return;
    const raw = xhr.responseText || '';
    const ct = (xhr.getResponseHeader('content-type') || '').toLowerCase();
    let body = raw;
    if (ct.indexOf('application/json') !== -1) {
      try { body = raw ? JSON.parse(raw) : {}; } catch (e) { body = { error: 'json parse error', raw: raw }; }
    }
    if (xhr.status < 200 || xhr.status >= 300) {
      const msg = typeof body === 'string' ? body : (body && (body.error || body.message)) || ('HTTP ' + xhr.status);
      done(new Error(msg));
      return;
    }
    done(null, body);
  };
  xhr.ontimeout = function () { done(new Error('timeout esperando ' + path)); };
  xhr.onerror = function () { done(new Error('fallo de red en ' + path)); };
  xhr.send(opts.body || null);
}

function activate(tab) {
  tabs.forEach((t) => t.classList.toggle('active', t.getAttribute('data-tab') === tab));
  Object.keys(panels).forEach((k) => panels[k].classList.toggle('hidden', k !== tab));
}

function loadWho() {
  api('/api/whoami', null, function (err, who) {
    setText(q('#who'), err ? 'Cliente: ?' : ('Cliente: ' + ((who && who.remote_addr) || '-')));
  });
}

function loadStatus() {
  api('/api/status', null, function (err, st) {
    if (err) {
      setText(q('#interfacesOut'), String(err.message || err));
      setText(q('#routesOut'), String(err.message || err));
      return;
    }
    const ipf = ((st.ip_forward && st.ip_forward.stdout) || '').trim();
    const ifOut = (st.interfaces && (st.interfaces.stdout || st.interfaces.stderr)) || '-';
    const rtOut = (st.routes && (st.routes.stdout || st.routes.stderr)) || '-';
    setText(q('#ipf'), ipf === '1' ? 'Habilitado' : (ipf || '-'));
    setText(q('#interfacesOut'), ifOut);
    setText(q('#routesOut'), rtOut);
    setText(q('#ifc'), String(ifOut.split('\n').filter(Boolean).length));
    setText(q('#rts'), String(rtOut.split('\n').filter(Boolean).length));
  });
}

function loadNft() { api('/api/nft/ruleset', null, (err, x) => setText(q('#nftOut'), err ? String(err.message || err) : (x.stdout || x.stderr || '-'))); }
function loadIpt() { api('/api/iptables/filter', null, (err, x) => setText(q('#iptOut'), err ? String(err.message || err) : (x.stdout || x.stderr || '-'))); }
function loadRoutesOnly() { api('/api/routes', null, (err, x) => setText(q('#routesOnlyOut'), err ? String(err.message || err) : (x.stdout || x.stderr || '-'))); }

function loadPorts() {
  api('/api/ports', null, function (err, out) {
    if (err) {
      setText(q('#portsOut'), String(err.message || err));
      return;
    }
    const rows = out.ports || [];
    if (!rows.length) {
      setText(q('#portsOut'), 'No hay puertos detectados.');
      return;
    }
    const lines = ['PROTO PORT  RX          TX          PROCESS'];
    lines.push('--------------------------------------------------------------');
    rows.forEach((r) => {
      lines.push(`${String(r.proto).padEnd(5)} ${String(r.port).padEnd(5)} ${String(r.received).padEnd(11)} ${String(r.sent).padEnd(11)} ${r.process || '-'}`);
    });
    setText(q('#portsOut'), lines.join('\n'));
  });
}

function loadVpnPeers() {
  api('/api/vpn/peers', null, function (err, out) {
    if (err) {
      setText(q('#vpnOut'), String(err.message || err));
      return;
    }
    const peers = out.peers || [];
    if (!peers.length) {
      setText(q('#vpnOut'), 'Sin peers registrados.');
      return;
    }
    const lines = ['NAME        ALLOWED IP       HANDSHAKE   RX          TX          ENDPOINT'];
    lines.push('------------------------------------------------------------------------------------------');
    peers.forEach((p) => {
      lines.push(`${String(p.name).padEnd(11)} ${String(p.allowed_ip).padEnd(16)} ${String(p.latest_handshake).padEnd(11)} ${String(p.received).padEnd(11)} ${String(p.sent).padEnd(11)} ${p.endpoint || '-'}`);
    });
    setText(q('#vpnOut'), lines.join('\n'));
  });
}

function formatDevices(devices) {
  if (!devices || !devices.length) return 'No hay dispositivos detectados.';
  const lines = [
    'Leyenda: ORIGEN=ip-neigh (tabla kernel), nmap-scan (descubierto por nmap), proc-arp (/proc/net/arp).',
    '',
    'IP                MAC                 IFACE      ESTADO      ORIGEN      DESCRIPCION'
  ];
  lines.push('---------------------------------------------------------------------------------------------------------------');
  devices.forEach((d) => {
    const ip = String(d.ip || '-');
    const desc = KNOWN_IP_DESCRIPTIONS[ip] || 'Equipo detectado (sin etiqueta)';
    lines.push(`${ip.padEnd(16)}${String(d.mac || '-').padEnd(20)}${String(d.iface || '-').padEnd(11)}${String(d.state || '-').padEnd(12)}${String(d.source || '-').padEnd(12)}${desc}`);
  });
  return lines.join('\n');
}

function formatScanSummary(scan) {
  if (!scan || !scan.nmap_available) {
    return 'Nmap: no disponible';
  }
  const nets = (scan.scanned_networks || []).length;
  const up = Number(scan.up_hosts_from_nmap || 0);
  const errs = (scan.errors || []).length;
  const elapsed = Number(scan.elapsed_seconds || 0).toFixed(1);
  const partial = scan.partial ? ' (parcial)' : '';
  return `Nmap ejecutado${partial}: redes=${nets}, hosts_up=${up}, errores=${errs}, tiempo=${elapsed}s`;
}

function loadDevices(activeScan) {
  if (typeof activeScan === 'boolean') devicesActiveMode = activeScan;
  let path = '/api/devices';
  let reqTimeout = 7000;
  if (devicesActiveMode) {
    path += '?active=1';
    setText(q('#devicesMode'), 'Modo: escaneo activo en curso...');
    reqTimeout = 45000;
  }
  api(path, { timeout: reqTimeout }, function (err, out) {
    if (err) {
      setText(q('#devicesOut'), String(err.message || err));
      return;
    }
    const scanSummary = devicesActiveMode ? formatScanSummary(out.scan) : 'Modo ARP (sin nmap).';
    const scanErrors = (out.scan && out.scan.errors && out.scan.errors.length)
      ? ('\nErrores nmap:\n- ' + out.scan.errors.join('\n- '))
      : '';
    const scanSkipped = (out.scan && out.scan.skipped_networks && out.scan.skipped_networks.length)
      ? ('\nRedes omitidas:\n- ' + out.scan.skipped_networks.join('\n- '))
      : '';
    const body = formatDevices(out.devices || []);
    setText(q('#devicesOut'), `${scanSummary}\n\n${body}${scanErrors}${scanSkipped}`);
    setText(q('#devc'), String(out.count || 0));
    const suffix = out.note ? (' | ' + out.note) : '';
    setText(q('#devicesMode'), 'Modo: ' + (out.mode || 'vecinos observados') + suffix);
  });
}

function loadAll() {
  loadStatus();
  loadWho();
  loadDevices(devicesActiveMode);
}

function restartTimer() {
  if (timer) clearInterval(timer);
  const sec = Number((q('#refreshRate') && q('#refreshRate').value) || 0);
  if (sec > 0) timer = setInterval(loadAll, sec * 1000);
}

tabs.forEach((btn) => {
  btn.addEventListener('click', function () {
    const tab = btn.getAttribute('data-tab');
    activate(tab);
    if (tab === 'nft') loadNft();
    if (tab === 'ipt') loadIpt();
    if (tab === 'routes') loadRoutesOnly();
    if (tab === 'devices') loadDevices(devicesActiveMode);
    if (tab === 'ports') loadPorts();
    if (tab === 'vpn') loadVpnPeers();
  });
});

q('#refreshAll').addEventListener('click', loadAll);
q('#refreshNft').addEventListener('click', loadNft);
q('#refreshIpt').addEventListener('click', loadIpt);
q('#refreshRoutes').addEventListener('click', loadRoutesOnly);
q('#refreshDevices').addEventListener('click', () => loadDevices(false));
q('#scanDevices').addEventListener('click', () => loadDevices(true));
q('#refreshPorts').addEventListener('click', loadPorts);
q('#refreshVpn').addEventListener('click', loadVpnPeers);
q('#refreshRate').addEventListener('change', restartTimer);
q('#reloadRules').addEventListener('click', loadNft);

q('#addVpn').addEventListener('click', function () {
  const name = (q('#vpnName').value || '').trim();
  if (!name) {
    setText(q('#vpnNewOut'), 'Indica un nombre de cliente.');
    return;
  }
  api('/api/vpn/add', {
    method: 'POST',
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: name })
  }, function (err, out) {
    if (err) {
      setText(q('#vpnNewOut'), String(err.message || err));
      return;
    }
    setText(q('#vpnNewOut'), out.client_conf || pretty(out));
    loadVpnPeers();
  });
});

q('#applyRule').addEventListener('click', function () {
  const rule = (q('#ruleInput').value || '').trim();
  if (!rule) {
    setText(q('#applyOut'), 'Escribe una regla nft valida.');
    return;
  }
  api('/api/nft/apply', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rule: rule })
  }, function (err, out) {
    if (err) {
      setText(q('#applyOut'), String(err.message || err));
      return;
    }
    setText(q('#applyOut'), pretty(out));
    loadNft();
  });
});

setText(q('#who'), 'Cliente: cargando...');
restartTimer();
loadAll();
