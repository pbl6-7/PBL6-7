import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GraduationCap, Loader2, ArrowLeft, Check, Lock, HelpCircle, KeyRound } from 'lucide-react';
import { getSecurityQuestionByUsername, verifySecurity, resetPassword } from '@/api/user';
import type { SecurityQuestion } from '@/types/user';

/** 找回密码步骤枚举 */
type Step = 1 | 2 | 3;

/** 找回密码页面组件，通过密保问题验证后重置密码 */
export default function ForgotPasswordPage() {
  const navigate = useNavigate();

  /* 步骤状态 */
  const [step, setStep] = useState<Step>(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  /* 步骤1：用户名 */
  const [username, setUsername] = useState('');

  /* 步骤2：密保问题与答案 */
  const [securityQuestion, setSecurityQuestion] = useState<SecurityQuestion | null>(null);
  const [securityAnswer, setSecurityAnswer] = useState('');

  /* 步骤3：新密码 */
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  /** 步骤1：提交用户名，获取密保问题 */
  const handleStep1 = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!username.trim()) {
      setError('请输入用户名');
      return;
    }
    if (username.trim().length < 3 || username.trim().length > 20) {
      setError('用户名需3-20个字符');
      return;
    }

    setLoading(true);
    try {
      const res = await getSecurityQuestionByUsername(username.trim());
      const question = res.data.data;
      if (!question || !question.question) {
        setError('该用户未设置密保问题，无法通过此方式找回密码');
        return;
      }
      setSecurityQuestion(question);
      setStep(2);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || '获取密保问题失败，请检查用户名');
    } finally {
      setLoading(false);
    }
  };

  /** 步骤2：验证密保答案 */
  const handleStep2 = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!securityAnswer.trim()) {
      setError('请输入密保答案');
      return;
    }
    if (securityAnswer.trim().length > 100) {
      setError('密保答案不能超过100个字符');
      return;
    }

    setLoading(true);
    try {
      await verifySecurity({ username: username.trim(), securityAnswer: securityAnswer.trim() });
      setStep(3);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || '密保答案验证失败');
    } finally {
      setLoading(false);
    }
  };

  /** 步骤3：重置密码 */
  const handleStep3 = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    /* 密码强度验证：至少8位，包含大小写字母、数字和特殊字符 */
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!passwordRegex.test(newPassword)) {
      setError('密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('两次输入的密码不一致');
      return;
    }

    setLoading(true);
    try {
      await resetPassword({
        username: username.trim(),
        securityAnswer: securityAnswer.trim(),
        newPassword,
      });
      /* 成功后跳转登录页 */
      navigate('/login', { state: { message: '密码重置成功，请使用新密码登录' } });
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || '重置密码失败');
    } finally {
      setLoading(false);
    }
  };

  /** 渲染步骤指示器 */
  const renderStepIndicator = () => (
    <div className="flex items-center justify-center mb-8">
      {[
        { n: 1, label: '输入用户名', icon: KeyRound },
        { n: 2, label: '验证密保', icon: HelpCircle },
        { n: 3, label: '重置密码', icon: Lock },
      ].map((s, i) => {
        const Icon = s.icon;
        const isCompleted = step > s.n;
        const isCurrent = step === s.n;
        return (
          <div key={s.n} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={`
                  w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold
                  transition-colors duration-200
                  ${isCompleted ? 'bg-green-500 text-white' : isCurrent ? 'bg-violet-600 text-white' : 'bg-violet-100 text-violet-400'}
                `}
              >
                {isCompleted ? <Check size={18} /> : <Icon size={18} />}
              </div>
              <span className={`mt-1.5 text-xs ${isCurrent ? 'text-violet-600 font-medium' : 'text-gray-400'}`}>
                {s.label}
              </span>
            </div>
            {i < 2 && (
              <div className={`w-12 h-0.5 mx-2 mb-5 ${step > s.n ? 'bg-green-500' : 'bg-violet-100'} transition-colors duration-200`} />
            )}
          </div>
        );
      })}
    </div>
  );

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#FAF5FF] px-4">
      <div className="max-w-md w-full">
        {/* Logo */}
        <div className="text-center mb-8">
          <GraduationCap size={40} className="mx-auto text-violet-600 mb-3" />
          <h1 className="text-3xl font-bold text-violet-600 mb-2">找回密码</h1>
          <p className="text-[#4C1D95]/60">通过密保问题验证重置您的密码</p>
        </div>

        {/* 表单卡片 */}
        <div className="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl border border-violet-100 p-8">
          {/* 步骤指示器 */}
          {renderStepIndicator()}

          {/* 错误提示 */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm">
              {error}
            </div>
          )}

          {/* 步骤1：输入用户名 */}
          {step === 1 && (
            <form onSubmit={handleStep1} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                  用户名
                </label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full px-4 py-2.5 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-sm text-[#4C1D95] placeholder:text-gray-400 transition-colors duration-200"
                  placeholder="请输入您的用户名"
                  required
                  minLength={3}
                  maxLength={20}
                  autoFocus
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-violet-600 text-white rounded-xl font-medium hover:bg-violet-700 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
              >
                {loading ? (
                  <>
                    <Loader2 className="animate-spin" size={18} />
                    查询中...
                  </>
                ) : (
                  '下一步'
                )}
              </button>
            </form>
          )}

          {/* 步骤2：回答密保问题 */}
          {step === 2 && (
            <form onSubmit={handleStep2} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                  密保问题
                </label>
                <div className="w-full px-4 py-2.5 border border-violet-200 rounded-xl bg-violet-50 text-sm text-violet-700">
                  {securityQuestion?.question || '加载中...'}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                  密保答案
                </label>
                <input
                  type="text"
                  value={securityAnswer}
                  onChange={(e) => setSecurityAnswer(e.target.value)}
                  className="w-full px-4 py-2.5 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-sm text-[#4C1D95] placeholder:text-gray-400 transition-colors duration-200"
                  placeholder="请输入密保答案"
                  required
                  maxLength={100}
                  autoFocus
                />
              </div>
              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => { setStep(1); setError(''); setSecurityAnswer(''); }}
                  className="flex-1 py-3 border border-violet-200 text-[#4C1D95] rounded-xl font-medium hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                >
                  上一步
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 py-3 bg-violet-600 text-white rounded-xl font-medium hover:bg-violet-700 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
                >
                  {loading ? (
                    <>
                      <Loader2 className="animate-spin" size={18} />
                      验证中...
                    </>
                  ) : (
                    '验证'
                  )}
                </button>
              </div>
            </form>
          )}

          {/* 步骤3：设置新密码 */}
          {step === 3 && (
            <form onSubmit={handleStep3} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                  新密码
                </label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-4 py-2.5 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-sm text-[#4C1D95] placeholder:text-gray-400 transition-colors duration-200"
                  placeholder="至少8位，包含大小写字母、数字和特殊字符"
                  required
                  autoFocus
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-[#4C1D95] mb-1.5">
                  确认密码
                </label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-4 py-2.5 border border-violet-200 rounded-xl bg-white/60 focus:ring-2 focus:ring-violet-400 focus:border-transparent outline-none text-sm text-[#4C1D95] placeholder:text-gray-400 transition-colors duration-200"
                  placeholder="请再次输入新密码"
                  required
                />
              </div>
              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => { setStep(2); setError(''); setNewPassword(''); setConfirmPassword(''); }}
                  className="flex-1 py-3 border border-violet-200 text-[#4C1D95] rounded-xl font-medium hover:bg-violet-50 transition-colors duration-200 cursor-pointer"
                >
                  上一步
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 py-3 bg-green-500 text-white rounded-xl font-medium hover:bg-green-600 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
                >
                  {loading ? (
                    <>
                      <Loader2 className="animate-spin" size={18} />
                      重置中...
                    </>
                  ) : (
                    '重置密码'
                  )}
                </button>
              </div>
            </form>
          )}

          {/* 返回登录 */}
          <div className="mt-6 text-center">
            <Link
              to="/login"
              className="inline-flex items-center gap-1.5 text-sm text-violet-600 hover:text-violet-700 transition-colors duration-200 cursor-pointer"
            >
              <ArrowLeft size={14} />
              返回登录
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
