import { create } from 'zustand';
import { useEffect, useState } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

/** 单条通知项接口 */
interface ToastItem {
  /** 唯一标识 */
  id: string;
  /** 通知类型 */
  type: 'success' | 'error' | 'warning' | 'info';
  /** 通知消息 */
  message: string;
}

/** Toast 状态管理接口 */
interface ToastStore {
  /** 当前通知列表 */
  toasts: ToastItem[];
  /** 添加一条通知 */
  addToast: (type: ToastItem['type'], message: string) => void;
  /** 移除指定通知 */
  removeToast: (id: string) => void;
}

/** Toast 全局状态 store */
const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  addToast: (type, message) => {
    const id = Date.now().toString() + Math.random().toString(36).slice(2);
    set((state) => ({
      toasts: [...state.toasts, { id, type, message }],
    }));
  },
  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((t) => t.id !== id),
    }));
  },
}));

/** 通知类型与图标、颜色的映射配置 */
const toastConfig: Record<
  ToastItem['type'],
  { icon: React.ElementType; bg: string; border: string; text: string; iconColor: string }
> = {
  success: {
    icon: CheckCircle,
    bg: 'bg-green-50',
    border: 'border-green-200',
    text: 'text-green-800',
    iconColor: 'text-green-500',
  },
  error: {
    icon: XCircle,
    bg: 'bg-red-50',
    border: 'border-red-200',
    text: 'text-red-800',
    iconColor: 'text-red-500',
  },
  warning: {
    icon: AlertTriangle,
    bg: 'bg-yellow-50',
    border: 'border-yellow-200',
    text: 'text-yellow-800',
    iconColor: 'text-yellow-500',
  },
  info: {
    icon: Info,
    bg: 'bg-violet-50',
    border: 'border-violet-200',
    text: 'text-violet-800',
    iconColor: 'text-violet-500',
  },
};

/** 单条通知组件 */
function ToastItemComponent({ toast, onClose }: { toast: ToastItem; onClose: () => void }) {
  const [visible, setVisible] = useState(false);
  const config = toastConfig[toast.type];
  const Icon = config.icon;

  /* 进入动画：挂载后触发滑入 */
  useEffect(() => {
    requestAnimationFrame(() => setVisible(true));
  }, []);

  /* 自动消失：3秒后滑出并移除 */
  useEffect(() => {
    const timer = setTimeout(() => {
      setVisible(false);
      setTimeout(onClose, 300);
    }, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <div
      className={`
        flex items-center gap-3 px-4 py-3 rounded-xl border shadow-lg
        ${config.bg} ${config.border}
        transform transition-all duration-300 ease-in-out
        ${visible ? 'translate-x-0 opacity-100' : 'translate-x-full opacity-0'}
      `}
    >
      <Icon size={20} className={config.iconColor} />
      <span className={`flex-1 text-sm font-medium ${config.text}`}>{toast.message}</span>
      <button
        onClick={() => { setVisible(false); setTimeout(onClose, 300); }}
        className="cursor-pointer p-1 rounded-lg hover:bg-black/5 transition-colors duration-200"
      >
        <X size={14} className="text-gray-400" />
      </button>
    </div>
  );
}

/** Toast 通知容器组件，渲染在页面右上角 */
export function Toast() {
  const toasts = useToastStore((s) => s.toasts);
  const removeToast = useToastStore((s) => s.removeToast);

  return (
    <div className="fixed top-6 right-6 z-[9999] flex flex-col gap-3 w-80">
      {toasts.map((toast) => (
        <ToastItemComponent
          key={toast.id}
          toast={toast}
          onClose={() => removeToast(toast.id)}
        />
      ))}
    </div>
  );
}

export { useToastStore };
