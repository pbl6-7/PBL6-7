import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Eye,
  EyeOff,
  LogIn,
  UserPlus,
  Loader2,
  GraduationCap,
  User,
  Lock,
  Mail,
  ShieldQuestion,
} from 'lucide-react';
import { login, register, getSecurityQuestions } from '@/api/user';
import type { LoginRequest, RegisterRequest, SecurityQuestion } from '@/types/user';
import { Toast, useToastStore } from '@/components/Toast';

/**
 * 密码强度等级配置
 */
interface PasswordStrength {
  /** 强度等级：弱、中、强 */
  level: 'weak' | 'medium' | 'strong' | 'empty';
  /** 显示文本 */
  label: string;
  /** 颜色样式类名 */
  style: string;
  /** 进度条宽度百分比 */
  width: string;
}

/**
 * 校园活动平台登录/注册页面组件
 * 提供用户认证功能，包含 Glassmorphism 风格的表单卡片
 */
export default function AuthPage() {
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  /* 登录表单状态 */
  const [loginForm, setLoginForm] = useState<LoginRequest>({
    username: '',
    password: '',
  });

  /* 注册表单状态 */
  const [registerForm, setRegisterForm] = useState<RegisterRequest>({
    username: '',
    password: '',
    realName: '',
    securityQuestionId: 0,
    securityAnswer: '',
  });

  /* 密保问题列表 */
  const [securityQuestions, setSecurityQuestions] = useState<SecurityQuestion[]>([]);
  const [loadingQuestions, setLoadingQuestions] = useState(false);

  /**
   * 加载密保问题列表
   * 从后端获取所有可选的安全问题
   */
  const loadSecurityQuestions = async () => {
    setLoadingQuestions(true);
    try {
      const res = await getSecurityQuestions();
      setSecurityQuestions(res.data.data);
      if (res.data.data.length > 0) {
        setRegisterForm(prev => ({ ...prev, securityQuestionId: res.data.data[0].questionId }));
      }
    } catch (err) {
      console.error('加载密保问题失败', err);
      useToastStore.getState().addToast('error', '加载密保问题失败');
    } finally {
      setLoadingQuestions(false);
    }
  };

  /**
   * 切换到注册标签页
   * 自动加载密保问题数据
   */
  const handleSwitchToRegister = () => {
    setIsLogin(false);
    setError('');
    if (securityQuestions.length === 0) {
      loadSecurityQuestions();
    }
  };

  /**
   * 处理登录表单提交
   * @param e - 表单事件对象
   */
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await login(loginForm);
      const { token, userId, username, role } = res.data.data;

      /* 保存 token 和用户信息到 localStorage */
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({ userId, username, role }));

      useToastStore.getState().addToast('success', '登录成功，欢迎回来！');
      navigate('/');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || '登录失败，请检查用户名和密码';
      setError(errorMessage);
      useToastStore.getState().addToast('error', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 计算密码强度等级
   * 根据密码长度和字符类型组合判断强度
   * @param password - 待检测的密码字符串
   * @returns 密码强度配置对象
   */
  const getPasswordStrength = (password: string): PasswordStrength => {
    if (!password) return { level: 'empty', label: '', style: '', width: '0%' };

    let score = 0;
    if (password.length >= 8) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score++;

    if (score <= 2) return { level: 'weak', label: '弱', style: 'bg-red-500', width: '33%' };
    if (score <= 3) return { level: 'medium', label: '中', style: 'bg-yellow-500', width: '66%' };
    return { level: 'strong', label: '强', style: 'bg-green-500', width: '100%' };
  };

  /**
   * 处理注册表单提交
   * 包含密码强度验证和安全检查
   * @param e - 表单事件对象
   */
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    /* 用户名校验：3-20位，仅允许字母数字下划线 */
    if (!/^[a-zA-Z0-9_]{3,20}$/.test(registerForm.username)) {
      setError('用户名需3-20位，仅允许字母、数字和下划线');
      useToastStore.getState().addToast('warning', '用户名格式不正确');
      return;
    }

    /* 真实姓名校验：2-20位 */
    if (registerForm.realName.trim().length < 2 || registerForm.realName.trim().length > 20) {
      setError('真实姓名需2-20个字符');
      useToastStore.getState().addToast('warning', '真实姓名长度不正确');
      return;
    }

    /* 密码强度验证：至少8位，包含大小写字母、数字和特殊字符 */
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!passwordRegex.test(registerForm.password)) {
      setError('密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符');
      useToastStore.getState().addToast('warning', '密码强度不足，请增强密码复杂度');
      return;
    }

    if (!registerForm.securityQuestionId || !registerForm.securityAnswer) {
      setError('请填写密保问题和答案');
      useToastStore.getState().addToast('warning', '请完成密保信息设置');
      return;
    }

    setLoading(true);

    try {
      await register(registerForm);
      useToastStore.getState().addToast('success', '注册成功！请使用新账号登录');
      setIsLogin(true);
      setLoginForm({ username: registerForm.username, password: '' });
    } catch (err: any) {
      /* 优先使用后端返回的错误消息 */
      const errorMessage = err.response?.data?.message || err.message || '注册失败';
      setError(errorMessage);
      useToastStore.getState().addToast('error', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  /* 获取当前密码强度（用于注册页面显示） */
  const pwdStrength = getPasswordStrength(registerForm.password);

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden bg-gradient-to-br from-surface-50 via-primary-50 to-accent-50 px-4">
      {/* 动画背景装饰元素 */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        {/* 大型渐变圆形 - 左上 */}
        <div className="absolute -top-40 -left-40 w-80 h-80 bg-primary-300/30 rounded-full blur-3xl animate-pulse-soft" />
        {/* 中等圆形 - 右上 */}
        <div className="absolute top-1/4 -right-20 w-64 h-64 bg-accent-400/20 rounded-full blur-3xl animate-pulse-soft" style={{ animationDelay: '1s' }} />
        {/* 小圆形 - 左下 */}
        <div className="absolute -bottom-20 left-1/4 w-48 h-48 bg-primary-400/25 rounded-full blur-2xl animate-pulse-soft" style={{ animationDelay: '0.5s' }} />
        {/* 装饰圆环 */}
        <div className="absolute top-16 right-1/4 w-32 h-32 border-2 border-primary-200/40 rounded-full" />
        <div className="absolute bottom-32 left-12 w-24 h-24 border border-primary-200/30 rounded-full" />
        <div className="absolute top-1/2 right-8 w-16 h-16 bg-primary-200/20 rounded-full" />
        {/* 浮动小点 */}
        <div className="absolute top-28 left-1/3 w-3 h-3 bg-accent-400/50 rounded-full animate-pulse-soft" />
        <div className="absolute bottom-20 right-1/3 w-4 h-4 bg-primary-400/40 rounded-full animate-pulse-soft" style={{ animationDelay: '1.5s' }} />
        <div className="absolute top-1/2 left-16 w-2 h-2 bg-primary-300/60 rounded-full animate-pulse-soft" style={{ animationDelay: '0.8s' }} />
      </div>

      <div className="max-w-md w-full relative z-10">
        {/* Logo 区域 */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-600 to-primary-800 rounded-2xl shadow-glow mb-4">
            <GraduationCap size={32} className="text-white" />
          </div>
          <h1 className="text-3xl font-heading font-bold text-text-primary mb-2">
            校园活动平台
          </h1>
          <p className="text-text-secondary font-body">
            发现精彩活动，参与校园生活
          </p>
        </div>

        {/* Glassmorphism 表单卡片 */}
        <div className="bg-white/90 backdrop-blur-xl border border-white/30 shadow-xl rounded-3xl p-8">
          {/* Pill 形式 Tab 切换器 */}
          <div className="flex mb-8 bg-primary-50 rounded-2xl p-1">
            <button
              type="button"
              onClick={() => { setIsLogin(true); setError(''); }}
              className={`flex-1 py-2.5 text-center font-medium rounded-xl transition-all duration-300 cursor-pointer ${
                isLogin
                  ? 'bg-white text-primary-700 shadow-button'
                  : 'text-text-muted hover:text-text-secondary'
              }`}
            >
              登录
            </button>
            <button
              type="button"
              onClick={handleSwitchToRegister}
              className={`flex-1 py-2.5 text-center font-medium rounded-xl transition-all duration-300 cursor-pointer ${
                !isLogin
                  ? 'bg-white text-primary-700 shadow-button'
                  : 'text-text-muted hover:text-text-secondary'
              }`}
            >
              注册
            </button>
          </div>

          {/* 错误提示 */}
          {error && (
            <div className="mb-5 p-3.5 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm animate-fadeInUp">
              {error}
            </div>
          )}

          {/* 登录表单 */}
          {isLogin ? (
            <form onSubmit={handleLogin} className="space-y-5">
              {/* 用户名输入框 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  用户名
                </label>
                <div className="relative">
                  <User size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    value={loginForm.username}
                    onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
                    className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary placeholder:text-gray-400"
                    placeholder="请输入用户名"
                    required
                  />
                </div>
              </div>

              {/* 密码输入框 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  密码
                </label>
                <div className="relative">
                  <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={loginForm.password}
                    onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                    className="w-full pl-11 pr-12 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary placeholder:text-gray-400"
                    placeholder="请输入密码"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-gray-600 transition-colors duration-200 cursor-pointer"
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              {/* 忘记密码链接 */}
              <div className="flex items-center justify-between text-sm">
                <Link
                  to="/forgot-password"
                  className="text-primary-600 hover:text-primary-700 transition-colors duration-200 cursor-pointer font-medium"
                >
                  忘记密码？
                </Link>
              </div>

              {/* 登录按钮 */}
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl font-semibold hover:from-primary-700 hover:to-primary-800 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer shadow-button hover:shadow-button-hover"
              >
                {loading ? (
                  <>
                    <Loader2 className="animate-spin" size={18} />
                    登录中...
                  </>
                ) : (
                  <>
                    <LogIn size={18} />
                    登录
                  </>
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-5">
              {/* 用户名输入框 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  用户名
                </label>
                <div className="relative">
                  <User size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    value={registerForm.username}
                    onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })}
                    className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary placeholder:text-gray-400"
                    placeholder="3-20位字母、数字或下划线"
                    required
                    minLength={3}
                    maxLength={20}
                  />
                </div>
              </div>

              {/* 真实姓名输入框 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  真实姓名
                </label>
                <div className="relative">
                  <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    value={registerForm.realName}
                    onChange={(e) => setRegisterForm({ ...registerForm, realName: e.target.value })}
                    className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary placeholder:text-gray-400"
                    placeholder="请输入真实姓名"
                    required
                    minLength={2}
                    maxLength={20}
                  />
                </div>
              </div>

              {/* 密码输入框 + 强度指示器 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  密码
                </label>
                <div className="relative">
                  <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="password"
                    value={registerForm.password}
                    onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                    className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary placeholder:text-gray-400"
                    placeholder="至少8位，包含大小写字母、数字和特殊字符"
                    required
                    minLength={8}
                  />
                </div>
                {/* 密码强度指示器 */}
                {registerForm.password && (
                  <div className="mt-2 space-y-1">
                    <div className="h-1.5 w-full bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className={`h-full ${pwdStrength.style} rounded-full transition-all duration-300 ease-out`}
                        style={{ width: pwdStrength.width }}
                      />
                    </div>
                    <span className={`text-xs font-medium ${
                      pwdStrength.level === 'weak' ? 'text-red-500' :
                      pwdStrength.level === 'medium' ? 'text-yellow-600' :
                      pwdStrength.level === 'strong' ? 'text-green-600' : ''
                    }`}>
                      密码强度：{pwdStrength.label}
                      {pwdStrength.level === 'weak' && ' — 建议增加字符类型'}
                      {pwdStrength.level === 'strong' && ' ✓ 安全'}
                    </span>
                  </div>
                )}
              </div>

              {/* 密保问题选择框 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  密保问题
                </label>
                <div className="relative">
                  <ShieldQuestion size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none z-10" />
                  <select
                    value={registerForm.securityQuestionId}
                    onChange={(e) => setRegisterForm({ ...registerForm, securityQuestionId: Number(e.target.value) })}
                    className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary appearance-none cursor-pointer disabled:opacity-50"
                    disabled={loadingQuestions}
                    required
                  >
                    <option value={0}>请选择密保问题</option>
                    {securityQuestions.map((q) => (
                      <option key={q.questionId} value={q.questionId}>
                        {q.question}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* 密保答案输入框 */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-1.5">
                  密保答案
                </label>
                <div className="relative">
                  <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    value={registerForm.securityAnswer}
                    onChange={(e) => setRegisterForm({ ...registerForm, securityAnswer: e.target.value })}
                    className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none transition-colors duration-200 bg-white/80 text-text-primary placeholder:text-gray-400"
                    placeholder="请输入密保答案"
                    required
                  />
                </div>
              </div>

              {/* 注册按钮 */}
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl font-semibold hover:from-primary-700 hover:to-primary-800 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer shadow-button hover:shadow-button-hover"
              >
                {loading ? (
                  <>
                    <Loader2 className="animate-spin" size={18} />
                    注册中...
                  </>
                ) : (
                  <>
                    <UserPlus size={18} />
                    注册
                  </>
                )}
              </button>
            </form>
          )}
        </div>

        {/* 底部提示文字 */}
        <p className="text-center mt-6 text-sm text-text-muted">
          继续即表示您同意我们的{' '}
          <Link to="/terms" className="text-primary-600 hover:text-primary-700 transition-colors duration-200 cursor-pointer font-medium">
            服务条款
          </Link>{' '}
          和{' '}
          <Link to="/privacy" className="text-primary-600 hover:text-primary-700 transition-colors duration-200 cursor-pointer font-medium">
            隐私政策
          </Link>
        </p>
      </div>

      {/* Toast 通知组件 */}
      <Toast />
    </div>
  );
}