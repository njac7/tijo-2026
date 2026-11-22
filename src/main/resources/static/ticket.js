(function () {
    const form = document.getElementById('order-form');
    if (!form) {
        return;
    }

    const data = form.dataset;
    const quantityInput = form.querySelector('input[name="quantity"]');
    const priceInput = form.querySelector('input[name="limitPrice"]');
    const priceField = document.getElementById('price-field');
    const quickPrices = document.getElementById('quick-prices');
    const submit = document.getElementById('submit-btn');
    const valueOutput = document.getElementById('order-value');
    const valueLabel = document.getElementById('value-label');
    const cashInfo = document.getElementById('info-cash');
    const sharesInfo = document.getElementById('info-shares');

    const money = new Intl.NumberFormat('pl-PL', {minimumFractionDigits: 2, maximumFractionDigits: 2});

    const checked = name => form.querySelector('input[name="' + name + '"]:checked').value;
    const toNumber = value => value === '' || value === undefined ? NaN : Number(value);

    function estimatedPrice(isBuy, isMarket) {
        if (!isMarket) {
            return toNumber(priceInput.value);
        }
        return toNumber(isBuy ? data.ask : data.bid);
    }

    function update() {
        const isBuy = checked('side') === 'BUY';
        const isMarket = checked('type') === 'MARKET';

        priceField.hidden = isMarket;
        quickPrices.hidden = isMarket;
        priceInput.required = !isMarket;

        cashInfo.classList.toggle('dim', !isBuy);
        sharesInfo.classList.toggle('dim', isBuy);

        submit.textContent = (isBuy ? 'Kup ' : 'Sprzedaj ') + data.ticker;
        submit.classList.toggle('submit-buy', isBuy);
        submit.classList.toggle('submit-sell', !isBuy);

        const quantity = toNumber(quantityInput.value);
        const price = estimatedPrice(isBuy, isMarket);
        valueLabel.textContent = isMarket ? 'Szacowana wartość (PKC)' : 'Wartość zlecenia';
        valueOutput.classList.remove('warn');
        if (!(quantity > 0) || !(price > 0)) {
            valueOutput.textContent = '—';
            return;
        }
        const value = quantity * price;
        valueOutput.textContent = money.format(value) + ' zł';
        const exceeds = isBuy ? value > Number(data.cash) : quantity > Number(data.shares);
        valueOutput.classList.toggle('warn', exceeds);
    }

    function setSide(side) {
        form.querySelector('input[name="side"][value="' + side + '"]').checked = true;
    }

    function setLimitPrice(price) {
        form.querySelector('input[name="type"][value="LIMIT"]').checked = true;
        priceInput.value = Number(price).toFixed(2);
    }

    document.querySelectorAll('.ladder-row').forEach(row => {
        row.addEventListener('click', () => {
            setSide(row.dataset.side);
            setLimitPrice(row.dataset.price);
            update();
            quantityInput.focus();
        });
    });

    quickPrices.querySelectorAll('button').forEach(button => {
        button.addEventListener('click', () => {
            const price = data[button.dataset.quick];
            if (price) {
                setLimitPrice(price);
                update();
            }
        });
    });

    form.addEventListener('input', update);
    form.addEventListener('change', update);
    update();
})();
