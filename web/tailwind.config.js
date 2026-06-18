/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      /* 颜色系统 - 基于紫色主题的校园活动平台设计规范 */
      colors: {
        /* 主色 - 活力紫 #7C3AED */
        primary: {
          50: '#f5f3ff',
          100: '#ede9fe',
          200: '#ddd6fe',
          300: '#c4b5fd',
          400: '#a78bfa',
          500: '#8b5cf6',
          600: '#7c3aed',
          700: '#6d28d9',
          800: '#5b21b6',
          900: '#4c1d95',
          950: '#2e1065',
        },
        /* 辅助色 - 浅紫 #A78BFA */
        secondary: {
          50: '#f5f3ff',
          100: '#ede9fe',
          200: '#ddd6fe',
          300: '#c4b5fd',
          400: '#a78bfa',
          500: '#8b5cf6',
          600: '#7c3aed',
          700: '#6d28d9',
          800: '#5b21b6',
          900: '#4c1d95',
          950: '#2e1065',
        },
        /* 行动号召/强调色 - 绿色 #22C55E */
        accent: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
          950: '#052e16',
        },
        /* 背景色 - 浅紫色调 #FAF5FF */
        surface: {
          50: '#faf5ff',
          100: '#f3e8ff',
          200: '#e9d5ff',
          300: '#d8b4fe',
          400: '#c084fc',
          500: '#a855f7',
          600: '#9333ea',
          700: '#7e22ce',
          800: '#6b21a8',
          900: '#581c87',
          950: '#3b0764',
        },
        /* 文本色 - 深紫 #4C1D95 */
        text: {
          primary: '#4c1d95',
          secondary: '#6d28d9',
          muted: '#8b5cf6',
          light: '#a78bfa',
          inverse: '#ffffff',
        },
      },
      /* 字体系统 */
      fontFamily: {
        /* 标题字体 - Poppins */
        heading: ['Poppins', 'sans-serif'],
        /* 正文字体 - Inter */
        body: ['Inter', 'sans-serif'],
      },
      /* 自定义动画 */
      animation: {
        'fadeIn': 'fadeIn 0.3s ease-in-out',
        'fadeInUp': 'fadeInUp 0.4s ease-out',
        'fadeInDown': 'fadeInDown 0.4s ease-out',
        'slideUp': 'slideUp 0.3s ease-out',
        'slideDown': 'slideDown 0.3s ease-out',
        'scaleIn': 'scaleIn 0.2s ease-out',
        'shimmer': 'shimmer 2s infinite linear',
        'pulse-soft': 'pulse-soft 2s infinite ease-in-out',
        'bounce-in': 'bounce-in 0.5s ease-out',
        'spin-slow': 'spin 3s linear infinite',
      },
      /* 自定义关键帧 */
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInDown: {
          '0%': { opacity: '0', transform: 'translateY(-20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        'pulse-soft': {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.7' },
        },
        'bounce-in': {
          '0%': { transform: 'scale(0.3)', opacity: '0' },
          '50%': { transform: 'scale(1.05)' },
          '70%': { transform: 'scale(0.9)' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
      },
      /* 自定义盒阴影 */
      boxShadow: {
        'card': '0 2px 8px rgba(124, 58, 237, 0.08)',
        'card-hover': '0 8px 24px rgba(124, 58, 237, 0.15)',
        'button': '0 2px 4px rgba(124, 58, 237, 0.2)',
        'button-hover': '0 4px 12px rgba(124, 58, 237, 0.3)',
        'modal': '0 16px 48px rgba(124, 58, 237, 0.2)',
        'dropdown': '0 4px 16px rgba(124, 58, 237, 0.12)',
        'glow': '0 0 20px rgba(124, 58, 237, 0.3)',
        'glow-accent': '0 0 20px rgba(34, 197, 94, 0.3)',
      },
      /* 自定义间距 */
      spacing: {
        '4.5': '1.125rem',
        '13': '3.25rem',
        '15': '3.75rem',
      },
      /* 自定义圆角 */
      borderRadius: {
        '4xl': '2rem',
      },
      /* 自定义过渡时间 */
      transitionDuration: {
        '400': '400ms',
        '600': '600ms',
      },
    },
  },
  plugins: [],
}
