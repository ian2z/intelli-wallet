const tipoConta = document.getElementById("tipo");
const grupoFechamento = document.getElementById("diaFechamentoGrupo");
const diaFechamento = document.getElementById("diaFechamento");

function atualizarDiaFechamento() {
    const cartao = tipoConta.value === "CARTAO";
    grupoFechamento.hidden = !cartao;
    diaFechamento.required = cartao;
    if (!cartao) {
        diaFechamento.value = "";
    }
}

tipoConta.addEventListener("change", atualizarDiaFechamento);
atualizarDiaFechamento();
