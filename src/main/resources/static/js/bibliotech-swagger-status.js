(() => {
  const seletor = ".bibliotech-auth-status";
  let perfilDoLogin = null;
  let falhaNoLogin = false;
  let saiuDaConta = false;

  function mostrarEstado() {
    const faixa = document.querySelector(seletor);
    if (!faixa) return;

    const chave = obterChaveAutorizada();
    faixa.className = "bibliotech-auth-status";

    if (chave && perfilDoLogin && chave === perfilDoLogin.apiKey) {
      faixa.classList.add("bibliotech-auth-status--ok");
      faixa.textContent = `Login confirmado: ${perfilDoLogin.papel} · plano ${perfilDoLogin.plano}. A chave foi inserida no Swagger; o servidor valida o acesso em cada operação.`;
    } else if (chave) {
      faixa.classList.add("bibliotech-auth-status--info");
      faixa.textContent = "Chave informada no Swagger. A API confirma a validade e as permissões ao executar cada operação.";
    } else if (perfilDoLogin) {
      faixa.classList.add("bibliotech-auth-status--info");
      faixa.textContent = `Login confirmado: ${perfilDoLogin.papel} · plano ${perfilDoLogin.plano}. Copie a apiKey da resposta e informe em Authorize.`;
    } else if (saiuDaConta) {
      faixa.classList.add("bibliotech-auth-status--info");
      faixa.textContent = "Sessão encerrada na API. Para remover a chave salva no Swagger, abra Authorize e clique em Logout.";
    } else if (falhaNoLogin) {
      faixa.classList.add("bibliotech-auth-status--erro");
      faixa.textContent = "Não foi possível entrar. Confira o e-mail e a senha na resposta da operação de login.";
    } else {
      faixa.classList.add("bibliotech-auth-status--neutro");
      faixa.textContent = "Para usar operações protegidas, faça login e informe a apiKey em Authorize. A API verifica as permissões em cada operação.";
    }
  }

  function obterChaveAutorizada() {
    const estado = window.ui?.getState?.();
    const autorizado = estado?.get?.("auth")?.get?.("authorized");
    const credencial = autorizado?.get?.("ApiKeyAuth") ?? autorizado?.ApiKeyAuth;
    const valor = credencial?.get?.("value") ?? credencial?.value;
    return typeof valor === "string" ? valor : "";
  }

  function prepararFaixa() {
    const cabecalho = document.querySelector(".bibliotech-banner");
    if (!cabecalho || document.querySelector(seletor)) return;

    const faixa = document.createElement("div");
    faixa.className = "bibliotech-auth-status bibliotech-auth-status--neutro";
    faixa.setAttribute("role", "status");
    faixa.setAttribute("aria-live", "polite");
    cabecalho.appendChild(faixa);
    mostrarEstado();
  }

  const requisicaoOriginal = window.fetch;
  window.fetch = function (...argumentos) {
    return requisicaoOriginal.apply(this, argumentos).then((resposta) => {
      const endereco = typeof argumentos[0] === "string" ? argumentos[0] : argumentos[0]?.url;
      const caminho = endereco ? new URL(endereco, window.location.href).pathname : "";

      if (caminho.endsWith("/api/auth/login")) {
        falhaNoLogin = !resposta.ok;
        saiuDaConta = false;
        if (resposta.ok) {
          resposta.clone().json().then((dados) => {
            perfilDoLogin = {
              apiKey: dados.apiKey,
              papel: dados.papel || "MEMBRO",
              plano: dados.plano || "BASE"
            };
            mostrarEstado();
          }).catch(() => {});
        } else {
          perfilDoLogin = null;
        }
        mostrarEstado();
      }

      if (caminho.endsWith("/api/auth/logout") && resposta.ok) {
        perfilDoLogin = null;
        falhaNoLogin = false;
        saiuDaConta = true;
        mostrarEstado();
      }

      return resposta;
    });
  };

  prepararFaixa();
  new MutationObserver(prepararFaixa).observe(document.documentElement, { childList: true, subtree: true });
  window.setInterval(mostrarEstado, 600);
})();
