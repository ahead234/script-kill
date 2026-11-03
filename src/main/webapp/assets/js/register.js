document.addEventListener('DOMContentLoaded', function () {
    var form = document.querySelector('[data-register-form]');
    if (!form) return;

    var fields = {
        username: form.querySelector('#username'),
        phone: form.querySelector('#phone'),
        password: form.querySelector('#password'),
        confirmPassword: form.querySelector('#confirmPassword'),
        agreed: form.querySelector('#agreed')
    };
    var submitButton = form.querySelector('[data-submit]');
    var submitting = false;

    var RULES = {
        username: function (value) {
            if (!value) return '请输入用户名';
            if (!/^[A-Za-z0-9_]{4,20}$/.test(value)) return '用户名需为 4-20 位字母、数字或下划线';
            return '';
        },
        phone: function (value) {
            var mobile = (value || '').trim();
            if (!mobile) return '请输入手机号';
            if (!/^[0-9]+$/.test(mobile)) return '手机号只能包含数字';
            if (mobile.length !== 11) return '手机号需为 11 位数字，当前 ' + mobile.length + ' 位';
            if (!/^1[3-9][0-9]{9}$/.test(mobile)) return '手机号格式不正确，应以 1 开头且第二位为 3-9';
            return '';
        },
        password: function (value) {
            if (!value) return '请输入密码';
            if (value.length < 8 || value.length > 50) return '密码长度需为 8-50 个字符';
            if (!/[A-Za-z]/.test(value) || !/[0-9]/.test(value)) return '密码需同时包含字母和数字';
            return '';
        },
        confirmPassword: function (value) {
            if (!value) return '请再次输入密码';
            if (value !== fields.password.value) return '两次输入的密码不一致';
            return '';
        },
        agreed: function () {
            return fields.agreed && fields.agreed.checked ? '' : '请先阅读并同意用户协议和隐私政策';
        }
    };

    function wrapperOf(name) {
        return form.querySelector('[data-field="' + name + '"]');
    }

    function showError(name, message) {
        var wrapper = wrapperOf(name);
        var output = form.querySelector('[data-error-for="' + name + '"]');
        if (output) {
            output.textContent = message || '';
            output.classList.toggle('is-visible', !!message);
        }
        if (wrapper) {
            wrapper.classList.toggle('has-error', !!message);
            wrapper.classList.toggle('is-valid', !message && !!valueOf(name));
        }
        if (fields[name] && fields[name].type !== 'checkbox') {
            if (message) {
                fields[name].setAttribute('aria-invalid', 'true');
            } else {
                fields[name].removeAttribute('aria-invalid');
            }
        }
    }

    function valueOf(name) {
        var field = fields[name];
        if (!field) return '';
        return field.type === 'checkbox' ? '' : field.value;
    }

    function validate(name) {
        var message = RULES[name](valueOf(name));
        showError(name, message);
        return !message;
    }

    function strengthOf(value) {
        if (!value || value.length < 8) return 1;
        var hasPair = /[A-Za-z]/.test(value) && /[0-9]/.test(value);
        if (!hasPair) return 1;
        return value.length >= 12 || /[^A-Za-z0-9]/.test(value) ? 3 : 2;
    }

    function renderStrength() {
        var meter = form.querySelector('[data-strength]');
        var fill = form.querySelector('[data-strength-fill]');
        var text = form.querySelector('[data-strength-text]');
        var value = fields.password.value;
        if (!value) {
            meter.hidden = true;
            fill.style.width = '0';
            return;
        }
        meter.hidden = false;
        var level = strengthOf(value);
        var labels = { 1: '弱', 2: '中', 3: '强' };
        fill.style.width = (level === 1 ? 34 : level === 2 ? 67 : 100) + '%';
        fill.setAttribute('data-level', level === 1 ? 'weak' : level === 2 ? 'medium' : 'strong');
        text.textContent = '密码强度：' + labels[level];
    }

    Object.keys(fields).forEach(function (name) {
        var field = fields[name];
        if (!field) return;
        var eventName = field.type === 'checkbox' ? 'change' : 'input';
        field.addEventListener(eventName, function () {
            // 手机号只接受数字，输入非数字字符时自动剔除，最多保留 11 位
            if (name === 'phone') {
                var digits = field.value.replace(/\D/g, '').slice(0, 11);
                if (digits !== field.value) field.value = digits;
            }
            if (name === 'password') {
                renderStrength();
                if (fields.confirmPassword.value) validate('confirmPassword');
            }
            validate(name);
        });
        field.addEventListener('blur', function () {
            if (field.type !== 'checkbox') validate(name);
        });
    });

    Array.prototype.forEach.call(form.querySelectorAll('[data-password-toggle]'), function (button) {
        button.addEventListener('click', function () {
            var input = document.getElementById(button.getAttribute('data-password-toggle'));
            if (!input) return;
            var reveal = input.type === 'password';
            input.type = reveal ? 'text' : 'password';
            button.textContent = reveal ? '隐藏' : '显示';
            button.setAttribute('aria-label', reveal ? '隐藏密码' : '显示密码');
            input.focus();
        });
    });

    form.addEventListener('submit', function (event) {
        if (submitting) {
            event.preventDefault();
            return;
        }
        var firstInvalid = null;
        Object.keys(RULES).forEach(function (name) {
            if (!validate(name) && !firstInvalid) firstInvalid = fields[name];
        });
        if (firstInvalid) {
            event.preventDefault();
            if (firstInvalid.focus) firstInvalid.focus();
            return;
        }
        submitting = true;
        submitButton.disabled = true;
        submitButton.setAttribute('aria-busy', 'true');
        submitButton.textContent = '注册中…';
    });

    // 浏览器回退缓存恢复页面时，重置按钮状态，避免按钮一直是禁用态
    window.addEventListener('pageshow', function () {
        submitting = false;
        submitButton.disabled = false;
        submitButton.removeAttribute('aria-busy');
        submitButton.textContent = '注册账号';
    });
});
