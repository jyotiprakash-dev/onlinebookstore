(function () {
    var emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    var namePattern = /^[A-Za-z][A-Za-z .'-]{0,99}$/;
    var phonePattern = /^[0-9]{10}$/;

    function byId(id) {
        return document.getElementById(id);
    }

    function trim(value) {
        return (value || "").trim();
    }

    function valueOf(id) {
        var element = byId(id);
        return element ? element.value : "";
    }

    function showErrors(errors) {
        if (errors.length > 0) {
            alert(errors.join("\n"));
            return false;
        }
        return true;
    }

    window.validateCustomerRegistration = function () {
        var fieldIds = ["Email", "passWord", "firstName", "lastName", "address", "phno", "acceptance"];
        var firstInvalidField = null;
        var isValid = true;

        for (var i = 0; i < fieldIds.length; i++) {
            var fieldId = fieldIds[i];
            var message = getRegistrationError(fieldId);

            setFieldError(fieldId, message);
            if (message) {
                isValid = false;
                if (!firstInvalidField) {
                    firstInvalidField = byId(fieldId);
                }
            }
        }

        if (firstInvalidField) {
            firstInvalidField.focus();
        }

        return isValid;
    };

    window.validateCustomerLogin = function () {
        var errors = [];
        var username = trim(valueOf("userName"));
        var password = valueOf("Password");

        if (!username) {
            errors.push("Username is required.");
        } else if (username.length > 100) {
            errors.push("Username must be 100 characters or less.");
        }

        if (!password) {
            errors.push("Password is required.");
        } else if (password.length > 100) {
            errors.push("Password must be 100 characters or less.");
        }

        validateCaptcha(errors);
        return showErrors(errors);
    };

    window.checkform = window.validateCustomerLogin;

    function validateCaptcha(errors) {
        var captchaInput = byId("CaptchaInput");
        var captchaValue = byId("txtCaptcha");

        if (!captchaInput || !captchaValue) {
            return;
        }

        if (!trim(captchaInput.value)) {
            errors.push("Please enter CAPTCHA code.");
        } else if (removeSpaces(captchaInput.value) !== removeSpaces(captchaValue.value)) {
            errors.push("The CAPTCHA code does not match.");
        }
    }

    function generateCaptcha() {
        var captchaInput = byId("txtCaptcha");
        var captchaDisplay = byId("CaptchaDiv");

        if (!captchaInput || !captchaDisplay) {
            return;
        }

        var code = "";
        for (var i = 0; i < 5; i++) {
            code += Math.ceil(Math.random() * 9);
        }
        captchaInput.value = code;
        captchaDisplay.innerHTML = code;
    }

    function removeSpaces(value) {
        return (value || "").split(" ").join("");
    }

    function getRegistrationError(fieldId) {
        var value = trim(valueOf(fieldId));

        if (fieldId === "Email") {
            if (!value || value.length > 100 || !emailPattern.test(value)) {
                return "Please enter a valid email address";
            }
            return "";
        }

        if (fieldId === "passWord") {
            if (!value) {
                return "Password is required";
            }
            if (value.length < 8) {
                return "Password must be at least 8 characters";
            }
            if (value.length > 100) {
                return "Password must be 100 characters or less";
            }
            return "";
        }

        if (fieldId === "firstName") {
            return getNameError("First name", value);
        }

        if (fieldId === "lastName") {
            return getNameError("Last name", value);
        }

        if (fieldId === "address") {
            if (!value) {
                return "Address is required";
            }
            if (value.length > 250) {
                return "Address must be 250 characters or less";
            }
            return "";
        }

        if (fieldId === "phno") {
            if (!phonePattern.test(value)) {
                return "Enter a valid 10-digit mobile number";
            }
            return "";
        }

        if (fieldId === "acceptance") {
            var acceptance = byId("acceptance");
            if (!acceptance || !acceptance.checked) {
                return "Please accept the Terms & Conditions";
            }
        }

        return "";
    }

    function getNameError(label, value) {
        if (!value) {
            return label + " is required";
        }
        if (value.length > 100) {
            return label + " must be 100 characters or less";
        }
        if (!namePattern.test(value)) {
            return label + " can contain only letters, spaces, apostrophes, periods, and hyphens";
        }
        return "";
    }

    function setFieldError(fieldId, message) {
        var error = byId(fieldId + "Error");
        var field = byId(fieldId);
        var wrapper = byId(fieldId + "Field");

        if (error) {
            error.textContent = message || "";
        }
        if (field) {
            field.setAttribute("aria-invalid", message ? "true" : "false");
        }
        if (wrapper) {
            if (message) {
                wrapper.classList.add("has-error");
            } else {
                wrapper.classList.remove("has-error");
            }
        }
    }

    function installRegistrationValidation() {
        var fieldIds = ["Email", "passWord", "firstName", "lastName", "address", "phno", "acceptance"];

        for (var i = 0; i < fieldIds.length; i++) {
            addRegistrationListener(fieldIds[i]);
        }
    }

    function addRegistrationListener(fieldId) {
        var field = byId(fieldId);

        if (!field) {
            return;
        }

        var eventName = fieldId === "acceptance" ? "change" : "input";
        field.addEventListener(eventName, function () {
            setFieldError(fieldId, getRegistrationError(fieldId));
        });

        if (fieldId !== "acceptance") {
            field.addEventListener("blur", function () {
                setFieldError(fieldId, getRegistrationError(fieldId));
            });
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        generateCaptcha();
        installRegistrationValidation();
    });
})();
