/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                'cyber-purple': '#a855f7',
                'cyber-blue': '#3b82f6',
                'cyber-green': '#10b981',
                'cyber-pink': '#ec4899',
                'dark-bg': '#0a0a0f',
                'dark-surface': '#13131a',
                'dark-elevated': '#1a1a24',
            },
            fontFamily: {
                sans: ['Inter', 'system-ui', 'sans-serif'],
                mono: ['JetBrains Mono', 'monospace'],
            },
            animation: {
                'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                'glow': 'glow 2s ease-in-out infinite',
            },
            keyframes: {
                glow: {
                    '0%, 100%': { opacity: 1 },
                    '50%': { opacity: 0.5 },
                },
            },
        },
    },
    plugins: [],
}
