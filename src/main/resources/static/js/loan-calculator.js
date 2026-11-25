(function () {
    const principalInput = document.getElementById("principalAmount");
    const rateInput = document.getElementById("annualInterestRate");
    const tenureInput = document.getElementById("tenureMonths");
    const emiOutput = document.getElementById("previewEmi");
    const totalOutput = document.getElementById("previewTotal");

    if (!principalInput || !rateInput || !tenureInput || !emiOutput || !totalOutput) {
        return;
    }

    function updatePreview() {
        const p = parseFloat(principalInput.value);
        const annualRate = parseFloat(rateInput.value);
        const n = parseInt(tenureInput.value, 10);

        if (isNaN(p) || p <= 0 || isNaN(n) || n <= 0) {
            emiOutput.textContent = "0.00";
            totalOutput.textContent = "0.00";
            return;
        }

        const r = isNaN(annualRate) || annualRate <= 0 ? 0 : annualRate / 1200;
        let emi;
        if (r === 0) {
            emi = p / n;
        } else {
            const factor = Math.pow(1 + r, n);
            emi = (p * r * factor) / (factor - 1);
        }

        const total = emi * n;
        emiOutput.textContent = emi.toFixed(2);
        totalOutput.textContent = total.toFixed(2);
    }

    principalInput.addEventListener("input", updatePreview);
    rateInput.addEventListener("input", updatePreview);
    tenureInput.addEventListener("input", updatePreview);
    updatePreview();
})();