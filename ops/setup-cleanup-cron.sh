#!/bin/bash
#
# setup-cleanup-cron.sh
# ---------------------
# Instala, no servidor de produção do tisaicore:
#   - /usr/local/bin/cleanup-disk.sh       (script de limpeza)
#   - /etc/cron.d/cleanup-disk             (agenda semanal — domingo 03:00)
#   - /etc/logrotate.d/cleanup-disk        (rotação do log de execução)
#
# Como usar (rodar como root no servidor):
#   chmod +x setup-cleanup-cron.sh
#   sudo ./setup-cleanup-cron.sh
#
# Pra mudar o horário, edita o cron depois em /etc/cron.d/cleanup-disk
# Formato do cron:  min hora dia-mês mês dia-semana
# Default: 0 3 * * 0  =  domingos às 03:00

set -euo pipefail

if [[ $EUID -ne 0 ]]; then
  echo "Este script precisa ser rodado como root (use sudo)."
  exit 1
fi

echo "==> Criando /usr/local/bin/cleanup-disk.sh"
cat > /usr/local/bin/cleanup-disk.sh <<'CLEANUP_EOF'
#!/bin/bash
# Limpeza semanal de disco — tisaicore
# Trunca logs Docker, remove imagens/cache não usados, vacuum journal, apt clean.
# NÃO mexe em volumes Docker (preserva o banco).
set -e

ts() { date '+%Y-%m-%d %H:%M:%S'; }
log() { echo "[$(ts)] $*"; }

before=$(df --output=avail -B1 / | tail -1)

log "===== INÍCIO ====="
log "Espaço livre antes: $(df -h / | awk 'NR==2 {print $4}')"

log "→ Truncando logs JSON dos containers Docker"
truncate -s 0 /var/lib/docker/containers/*/*-json.log 2>/dev/null || true

log "→ Removendo imagens/build cache/networks não usados (NÃO mexe em volumes)"
docker system prune -af 2>&1 | tail -5

log "→ Vacuum do journalctl (mantém últimos 100MB)"
journalctl --vacuum-size=100M 2>&1 | tail -3

log "→ apt-get clean"
apt-get clean

after=$(df --output=avail -B1 / | tail -1)
freed=$(( (after - before) / 1024 / 1024 ))
log "Espaço livre depois: $(df -h / | awk 'NR==2 {print $4}')"
log "Liberado: ${freed} MB"
log "===== FIM ====="
CLEANUP_EOF
chmod +x /usr/local/bin/cleanup-disk.sh

echo "==> Criando /etc/cron.d/cleanup-disk (domingo 03:00)"
cat > /etc/cron.d/cleanup-disk <<'CRON_EOF'
# Limpeza semanal de disco — tisaicore
# Roda todo domingo às 03:00. Saída vai pra /var/log/cleanup-disk.log
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

0 3 * * 0 root /usr/local/bin/cleanup-disk.sh >> /var/log/cleanup-disk.log 2>&1
CRON_EOF
chmod 644 /etc/cron.d/cleanup-disk

echo "==> Criando /etc/logrotate.d/cleanup-disk"
cat > /etc/logrotate.d/cleanup-disk <<'LOGROTATE_EOF'
/var/log/cleanup-disk.log {
    monthly
    rotate 6
    compress
    missingok
    notifempty
    create 0644 root root
}
LOGROTATE_EOF
chmod 644 /etc/logrotate.d/cleanup-disk

echo "==> Forçando reload do cron"
if command -v systemctl >/dev/null 2>&1; then
  systemctl restart cron 2>/dev/null || systemctl restart crond 2>/dev/null || true
fi

echo
echo "✓ Instalação concluída."
echo
echo "Arquivos criados:"
echo "  /usr/local/bin/cleanup-disk.sh"
echo "  /etc/cron.d/cleanup-disk"
echo "  /etc/logrotate.d/cleanup-disk"
echo
echo "Pra testar agora:   /usr/local/bin/cleanup-disk.sh"
echo "Pra ver o log:      tail -f /var/log/cleanup-disk.log"
echo "Pra mudar horário:  edite /etc/cron.d/cleanup-disk"
