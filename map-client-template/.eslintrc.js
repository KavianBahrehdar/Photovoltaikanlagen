module.exports = {
    parser: "@typescript-eslint/parser",
    extends: [
      "plugin:@typescript-eslint/recommended", // Uses the recommended rules from the @typescript-eslint/eslint-plugin
    ],
    parserOptions: {
      ecmaVersion: 2018,
      sourceType: "module"
    },
    rules: {
      "quotes": [2, "double", { "avoidEscape": true }],
      "@typescript-eslint/explicit-function-return-type": "off"
    }
  };