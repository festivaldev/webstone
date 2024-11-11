module.exports = {
  root: true,
  extends: ['airbnb-base', 'prettier'],
  rules: {
    'class-methods-use-this': 'off',
    'default-param-last': 'warn',
    'func-names': 'off',
    'global-require': 'off',
    'guard-for-in': 'warn',
    'import/extensions': 'off',
    'import/no-absolute-path': 'warn',
    'import/no-dynamic-require': 'off',
    'import/no-extraneous-dependencies': 'off',
    'import/no-unresolved': 'off',
    'import/order': 'warn',
    'import/prefer-default-export': 'warn',
    'lines-between-class-members': 'warn',
    'max-classes-per-file': 'warn',
    'max-len': 'off',
    'no-await-in-loop': 'off',
    'no-bitwise': 'off',
    'no-console': 'off',
    'no-empty': 'off',
    'no-lonely-if': 'off',
    'no-param-reassign': 'off',
    'no-plusplus': 'off',
    'no-restricted-globals': 'off',
    'no-restricted-syntax': 'off',
    'no-shadow': 'warn',
    'no-undef': 'off',
    'no-underscore-dangle': 'off',
    'no-unused-vars': 'off',
    'no-use-before-define': 'off',
    'prettier/prettier': 'error',
  },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2020,
    tsconfigRootDir: __dirname,
  },
  plugins: ['@typescript-eslint', 'prettier'],
};
