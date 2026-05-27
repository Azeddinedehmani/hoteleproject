/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ["./index.html","./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50:'#f0f4ff',100:'#dbe4ff',200:'#bac8ff',300:'#91a7ff',
          400:'#748ffc',500:'#5c7cfa',600:'#4c6ef5',700:'#3b5bdb',
          800:'#3051cf',900:'#2845a3',950:'#1a2e7a'
        },
        surface: {
          50:'#f8f9fc',100:'#f1f3f9',200:'#e2e8f0',
          800:'#1e293b',900:'#0f172a',950:'#080f1e'
        },
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans','sans-serif'],
        display: ['Syne','sans-serif'],
      },
      boxShadow: {
        'card':'0 1px 3px 0 rgba(0,0,0,.07),0 4px 16px -2px rgba(0,0,0,.05)',
        'card-hover':'0 4px 20px -2px rgba(92,124,250,.2)',
        'sidebar':'4px 0 24px -4px rgba(0,0,0,.12)',
        'modal':'0 25px 50px -12px rgba(0,0,0,.25)',
      },
      keyframes: {
        'fade-in': {'0%':{opacity:'0',transform:'translateY(8px)'},'100%':{opacity:'1',transform:'translateY(0)'}},
      },
      animation: { 'fade-in': 'fade-in 0.4s ease-out' },
    },
  },
  plugins: [],
}