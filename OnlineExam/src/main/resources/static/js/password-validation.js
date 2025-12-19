
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{}|;:,.<>?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{8,}$/;

const PASSWORD_RULES = {
    minLength: { regex: /.{8,}/, message: 'حداقل ۸ کاراکتر' },
    uppercase: { regex: /[A-Z]/, message: 'حداقل یک حرف بزرگ (A-Z)' },
    lowercase: { regex: /[a-z]/, message: 'حداقل یک حرف کوچک (a-z)' },
    digit: { regex: /\d/, message: 'حداقل یک عدد (0-9)' },
    special: { regex: /[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/, message: 'حداقل یک کاراکتر خاص (!@#$%^&*...)' },
    noSpaces: { regex: /^[^\s].*[^\s]$|^[^\s]$/, message: 'بدون فاصله در ابتدا و انتها' }
};

function validatePassword(password) {
    const results = {};
    let isValid = true;

    for (const [rule, config] of Object.entries(PASSWORD_RULES)) {
        const passed = config.regex.test(password);
        results[rule] = { passed, message: config.message };
        if (!passed) isValid = false;
    }

    return { isValid, results };
}

function createPasswordFeedbackUI(inputElement) {

    const feedbackDiv = document.createElement('div');
    feedbackDiv.className = 'password-feedback mt-2';
    feedbackDiv.innerHTML = `
        <small class="text-muted d-block mb-1">رمز عبور باید شامل موارد زیر باشد:</small>
        <div class="password-rules">
            ${Object.entries(PASSWORD_RULES).map(([rule, config]) => `
                <div class="rule-item" data-rule="${rule}">
                    <span class="rule-icon">○</span>
                    <span class="rule-text">${config.message}</span>
                </div>
            `).join('')}
        </div>
    `;

    const style = document.createElement('style');
    style.textContent = `
        .password-feedback {
            font-size: 0.85rem;
        }
        .password-rules {
            display: flex;
            flex-wrap: wrap;
            gap: 5px;
        }
        .rule-item {
            display: flex;
            align-items: center;
            gap: 4px;
            padding: 2px 8px;
            border-radius: 4px;
            background: #f8f9fa;
            border: 1px solid #dee2e6;
        }
        .rule-item.passed {
            background: #d4edda;
            border-color: #c3e6cb;
            color: #155724;
        }
        .rule-item.passed .rule-icon {
            color: #28a745;
        }
        .rule-item.failed {
            background: #f8d7da;
            border-color: #f5c6cb;
            color: #721c24;
        }
        .rule-item.failed .rule-icon {
            color: #dc3545;
        }
        .rule-icon {
            font-weight: bold;
        }
    `;
    document.head.appendChild(style);

    inputElement.parentNode.appendChild(feedbackDiv);
    return feedbackDiv;
}

function setupPasswordValidation(inputId, formId, isOptional = false) {
    const input = document.getElementById(inputId);
    const form = document.getElementById(formId);
    
    if (!input) return;

    const feedbackDiv = createPasswordFeedbackUI(input);
    

    feedbackDiv.style.display = 'none';

    input.addEventListener('focus', function() {
        if (!isOptional || this.value.length > 0) {
            feedbackDiv.style.display = 'block';
        }
    });

    input.addEventListener('input', function() {
        const password = this.value;
        
        if (isOptional && password.length === 0) {
            feedbackDiv.style.display = 'none';
            this.classList.remove('is-invalid', 'is-valid');
            return;
        }

        feedbackDiv.style.display = 'block';
        const { isValid, results } = validatePassword(password);

        for (const [rule, result] of Object.entries(results)) {
            const ruleElement = feedbackDiv.querySelector(`[data-rule="${rule}"]`);
            if (ruleElement) {
                ruleElement.classList.remove('passed', 'failed');
                ruleElement.classList.add(result.passed ? 'passed' : 'failed');
                ruleElement.querySelector('.rule-icon').textContent = result.passed ? '✓' : '✗';
            }
        }

        this.classList.remove('is-invalid', 'is-valid');
        this.classList.add(isValid ? 'is-valid' : 'is-invalid');
    });

    if (form) {
        form.addEventListener('submit', function(e) {
            const password = input.value;
            
            if (isOptional && password.length === 0) {
                return;
            }

            const { isValid } = validatePassword(password);
            if (!isValid) {
                e.preventDefault();
                input.classList.add('is-invalid');
                feedbackDiv.style.display = 'block';
                input.focus();
                alert('رمز عبور معتبر نیست. لطفاً تمام شرایط را رعایت کنید.');
            }
        });
    }
}

